TODO: see how Heroku passes on URLs to web-apps - since https is handled by Heroku and not the web-app.

TODO: include a '<' in a full name and see what happens.

Admin user
----------

Initially there is only one user (called 'admin') and this user cannot login until a password is set for them.

First generate a salt/hash value for the chosen password like so (replace `<password>` here with something suitably hard):
```
$ java -cp target/*/WEB-INF/lib/jetty-util-*.jar org.eclipse.jetty.util.security.Password admin <password>
```

Then take the `CRYPT:` value that was output and update the admin user password in the DB like so:
```
$ echo "UPDATE users SET password='CRYPT:...' WHERE username='admin'" | heroku pg:psql
```

Check the value is as expected:
````
$ echo 'SELECT * FROM users' | heroku pg:psql
```

Creating new users
------------------

The admin user can create new users (with the role `user`) using the page `/users`.

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

During testing you probably don't want SSL enabled, put once deployed you should ensure that the login pages and private content are only available via https.

This is done with the `<transport-guarantee>` tag like so:

```xml
<security-constraint>
    <web-resource-collection>
      <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <user-data-constraint>
      <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
  </security-constraint>
</web-app>
```

See e.g. https://click.apache.org/docs/user-guide/html/ch06.html for more details.

Error handling
--------------

All errors currently result in the web user either seeing an exception stack trace or a very basic default error screen.

E.g. if you are logged in as a normal user and try to access a page which require the admin role you'll see the cryptic default error page provided by jetty.

Using web.xml you can specify custom error pages for error codes (404 etc.) and exceptions (ServletException, arbitrary runtime exceptions).

Jetty also provides its own somewhat more flexible XML configuration for this.

See:

    http://eclipse.org/jetty/documentation/current/custom-error-pages.html
    http://stackoverflow.com/questions/7066192/how-to-specify-the-default-error-page-in-web-xml
    http://www.tutorialspoint.com/servlets/servlets-exception-handling.htm
    http://stackoverflow.com/a/15973954/245602

Some/most errors probably shouldn't be propagated as exceptions to the web user and should be better handled in the code.

Favicon
-------

The favicon is currently a black and white image of a question mark in a box and was generated using favicon generator as per [SO](http://stackoverflow.com/a/19590415/245602).

Page generations
----------------

Currently any page generation is handled through basic string manipulation without the help of any templating library or such like.

Page generation should be handled using something like (server side) JSF and/or (client side) AngularJS.

There are various projects that pull together JSF and Bootstrap, ditto for AngularJS.

AngularJS with Bootstrap seems to be covered by one clearly dominant project - http://angular-ui.github.io/bootstrap/

For JSF it's a little less clear - PrimeFaces seems to be the dominant modern JSF library and it supports a Bootstrap theme:

    http://www.primefaces.org/gettingStarted

There are alternatives, e.g. see:

    http://www.bootsfaces.net/page/examples/index.xhtml
    http://blog.hatemalimam.com/jsf-and-twitter-bootstrap-integration/
    https://github.com/pfroy/Bootstrap-JSF2.2

For some comentary see http://stackoverflow.com/a/25636220/245602

To get started with JSF see:

    http://www.tutorialspoint.com/jsf/jsf_quick_guide.htm
    http://docs.oracle.com/javaee/6/tutorial/doc/gjaam.html
    http://docs.oracle.com/javaee/7/tutorial/jsf-page.htm

See also the JSF 2 and PrimeFaces tutorials at:

    http://www.coreservlets.com/JSF-Tutorial/jsf2/#Tutorial-Intro

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
