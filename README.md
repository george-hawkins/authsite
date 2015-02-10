Website with authentication
===========================

This project provides a very simple Jetty based webapp that can deliver a static website with public and private sections.

Everything under `src/main/webapp/static` that is not in the `private` subdirectory can be viewed by anyone.

Anything under `src/main/webapp/static/private` can only be seen if the viewer has logged in.

User details are stored in a DB.

Users can modify their own details (via the `/userSettings` page) and the admin user (see below) can create, modify and delete users.

This project is intended to run on Heroku and has some Heroku specific logic (related to accessing the DB), but it could easily be adapted for a non-Heroku environment.

Installation
------------

To create DB table:

```bash
$ cat create-tables.ddl | heroku pg:psql
$ cat create-users.ddl | heroku pg:psql
```

To build:

```bash
$ mvn --quiet package -DskipTests
```

To run:

```bash
$ export DATABASE_URL=$(heroku config:get DATABASE_URL)
$ DATABASE_URL="$DATABASE_URL?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"
$ java -cp 'target/dependency/*' org.eclipse.jetty.runner.Runner src/main/config/etc/root-context.xml
```

Note: using `java -jar .../jetty-runner.jar ...` doesn't work here as `-jar` only honors the classpath in the specified jar's manifest and we want to add the DB driver jar.

Admin user
----------

Initially there is only one user (called 'admin') and this user cannot login until a password is set for them.

First generate a salt/hash value for the chosen password like so (replace `<password>` here with something suitably hard):
```
$ java -cp target/*/WEB-INF/lib/jetty-util-*.jar org.eclipse.jetty.util.security.Password admin <password>
```
TODO: update.

Then take the `CRYPT:` value that was output and update the admin user password in the DB like so:
```bash
$ echo "UPDATE users SET password='CRYPT:...' WHERE username='admin'" | heroku pg:psql
```

Check the value is as expected:
````bash
$ echo 'SELECT * FROM users' | heroku pg:psql
```

The admin user can now create additional users (with the role `user`) using the page `/users`.

Password handling
-----------------

The password logic uses `org.eclipse.jetty.util.security.Credential.Crypt.crypt(String, String)`.

This is the strongest of the 3 available options - the others being ROT13 like obfuscation and MD5 hashing.

Crypt uses the first two characters of the username as a salt (these can be seen at the start of the generated values) and encrypts the password using DES.

See "[Traditional DES-based scheme](http://en.wikipedia.org/wiki/Crypt_%28C%29#Traditional_DES-based_scheme)" on Wikipedia for more information.

Database backup
---------------

The database can be backed up on Heroku like so:

```bash
$ heroku pgbackups:capture --expire
```

The `--expire` causes the oldest existing backup to be expired. You can list the backups that Heroku is maintaining for you like so:

```bash
$ heroku pgbackups
```

Each backup is shown with an ID, to restore a particular one, e.g. with ID b001, do:

```bash
$ heroku pgbackups:restore DATABASE_URL b001
```

To retrieve a local copy of the latest backup do:

```bash
$ curl -o db.dump $(heroku pgbackups:url)
```

Restoring a locally stored backup isn't so simple - it first needs to be made available via a publicly accessable URL, e.g. a temporary Amazon S3 URL.

Once this is done it can be restored like so:

```bash
$ heroku pgbackups:restore DATABASE 'https://s3.amazonaws.com/.../db.dump'
```

See Heroku's [pgbackups](https://devcenter.heroku.com/articles/pgbackups) and [import/export](https://devcenter.heroku.com/articles/heroku-postgres-import-export) pages for more details.

SSL
---

TODO: update

When run locally pages can be accessed via http or https but when deployed to Heroku all non-https requests are redirected to https.

See the `-Dssl.only=true` in `Procfile` and how it affects the behavior of the class `HttpsRedirectFilter`.

Normally one would achieve such redirection by configuring one or more `CONFIDENTIAL` `transport-guarantee` constraints in `web.xml` along with some container specific configuration, e.g. as described [here](http://wiki.eclipse.org/Jetty/Howto/Configure_SSL#Redirecting_http_requests_to_https) in the Jetty wiki.

However Heroku does what's called SSL offloading hence a filter is required rather than a security constraint.

Jekyll
------

There is a [separate branch](https://github.com/george-hawkins/authsite/tree/jekyll) that uses Jekyll to build the website, see the [`src/main/jekyll` directory](https://github.com/george-hawkins/authsite/tree/jekyll/src/main/jekyll) on that branch and the [`HEROKU.md` file](https://github.com/george-hawkins/authsite/blob/jekyll/HEROKU.md) that describes the extra steps needed when using Jekyll with Heroku.

Error handling
--------------

All errors currently result in the web user either seeing an exception stack trace or a very basic default error screen.

E.g. if you are logged in as a normal user and try to access a page which require the admin role you'll see the cryptic default error page provided by jetty.

Using web.xml you can specify custom error pages for error codes (404 etc.) and exceptions (ServletException, arbitrary runtime exceptions).

Jetty also provides its own somewhat more flexible XML configuration for this.

See:

* http://eclipse.org/jetty/documentation/current/custom-error-pages.html
* http://stackoverflow.com/questions/7066192/how-to-specify-the-default-error-page-in-web-xml
* http://www.tutorialspoint.com/servlets/servlets-exception-handling.htm
* http://stackoverflow.com/a/15973954/245602

Some/most errors probably shouldn't be propagated as exceptions to the web user and should be better handled in the code.

Favicon
-------

The favicon is currently a black and white image of a question mark in a box and was generated using favicon generator as per [SO](http://stackoverflow.com/a/19590415/245602).

Page generation
---------------

Currently any page generation is handled through basic string manipulation without the help of any templating library or such like.

Page generation should be handled using something like (server side) JSF and/or (client side) AngularJS.

There are various projects that pull together JSF and Bootstrap, ditto for AngularJS.

AngularJS with Bootstrap seems to be covered by one clearly dominant project - http://angular-ui.github.io/bootstrap/

For JSF it's a little less clear - PrimeFaces seems to be the dominant modern JSF library and it supports a Bootstrap theme:

* http://www.primefaces.org/gettingStarted

There are alternatives, e.g. see:

* http://www.bootsfaces.net/page/examples/index.xhtml
* http://blog.hatemalimam.com/jsf-and-twitter-bootstrap-integration/
* https://github.com/pfroy/Bootstrap-JSF2.2

For some comentary see http://stackoverflow.com/a/25636220/245602

To get started with JSF see:

* http://www.tutorialspoint.com/jsf/jsf_quick_guide.htm
* http://docs.oracle.com/javaee/6/tutorial/doc/gjaam.html
* http://docs.oracle.com/javaee/7/tutorial/jsf-page.htm

See also the JSF 2 and PrimeFaces tutorials at:

* http://www.coreservlets.com/JSF-Tutorial/jsf2/#Tutorial-Intro

Caching
-------

There is currently no explicit handling of page caching. If it becomes an issues you can:

1. Disable caching of servlet responses - http://stackoverflow.com/questions/3413036/http-response-caching
2. Create a filter that disables caching - http://www.onjava.com/pub/a/onjava/2004/03/03/filters.html
3. Disable caching with `<meta>` tags - http://stackoverflow.com/a/1341133/245602

S3 content
----------

Currently all the static content is stored under `src/main/webapp/static`.

To release new content it has to be checked in and the app redeployed to Heroku.

In short Heroku isn't great for static assets. The suggested solution is store them insteads on S3 and protect them with "query string authorization".

Such assets can then be served using JetS3t Gatekeeper - see the basic authorization section of http://www.jets3t.org/applications/gatekeeper-concepts.html

On Linux you can interact with S3 buckets directly by mounting them locally using [s3fs-fuse](https://github.com/s3fs-fuse/s3fs-fuse/wiki/Fuse-Over-Amazon).

Shiro
-----

Using the Jetty container specific authentication logic and baking in various bits of user management stuff around it just for this project was probably a bad idea.

Using something like Shiro would probably be a more sensible long term approach:

* http://shiro.apache.org/webapp-tutorial.html
