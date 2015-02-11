website with authentication
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

If you compare the `java` command here with what you find in `Procfile` you'll see that `jetty.port` is specified and `jetty.xml` is used, `jetty.xml` is not required if running locally (and `jetty.port` is only needed by `jetty.xml`).

Admin user
----------

Initially there is only one user (called 'admin') and this user cannot login until a password is set for them.

First generate a salt/hash value for the chosen password like so (replace `<password>` here with something suitably hard):
```
$ java -cp target/*/WEB-INF/lib/jetty-util-*.jar org.eclipse.jetty.util.security.Password admin <password>
```

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

Heroku does what's called SSL offloading, this means that SSL is handled by the Heroku infrastructure and for the underlying web server, in this case Jetty, everything looks like a plain http request.

Whether the original request was https or http is communicated to Jetty by Heroku through the addition of an `x-forwarded-proto` header to all incoming requests.

In order to get Jetty to look for this header and use it to hide the whole issue of SSL offloading from the webapp the Jetty customizer `ForwardedRequestCustomizer` needs to be used.

Adding `ForwardedRequestCustomizer` is done in the file `jetty.xml` (which is then referenced in `Procfile`).

Once this is done one can require all requests to come via https using the normal `CONFIDENTIAL` `transport-guarantee` mechanism in `web.xml`.

If you look at `web.xml` you will see such constraints - maven filtering is used to set the `transport-guarantee` values to `NONE` if built locally, i.e. https is not used, or to `CONFIDENTIAL` if built on Heroku.

On its own setting `transport-guarantee` to `CONFIDENTIAL` causes Jetty to generate a FORBIDDEN (403) response to a request that tries to retrieve a given resource using http.

More useful would be if http requests were simply redirected to the corresponding https request.

Jetty doesn't provide this functionality out-of-the-box, rather a custom handler for FORBIDDEN has to be specified in `web.xml` using the `error-page` mechanism.

This handler is called `HttpsRedirectServlet` and if you look at it you'll see that it does a redirect if the FORBIDDEN reason can be traced to http vs https, otherwise it generates the error response one would have seen if it wasn't present at all.

Unfortunately if it creates an error response Jetty's default error generation logic will specify the URL of `HttpsRedirectServlet` as the source of the problem, rather than the original request that was forwarded to `HttpsRedirectServlet`.

To solve this cosmetic issue some Jetty specific logic is required, this is provided by `HttpsRedirectErrorPageHandler` (which is referenced by `root-context.xml`). This just subclasses the standard Jetty error page handler and adds logic to reference the original forwarded request where appropriate.

Note: `HttpsRedirectServlet` will only redirect to https if the system property `https.redirect` is true. This is property is set in `Procfile` and is left unset elsewhere - this is done because the redirect logic produces quite confusing behavior if it kicks in in an environment where https is not available.

Previously redirecting all requests to https was handled with a filter (in the fashion described in this [Jetty on Heroku](http://stackoverflow.com/questions/11564638/enforce-https-with-embedded-jetty-on-heroku/) question on StackOverflow).

However this has serious issues:

* if you don't use `ForwardedRequestCustomizer` then all your servlet logic will see all requests as being http, so any redirect done in a servlet will result in a redirect to a http resource, which only when requested by the client will result in the filter intercepting things and redirecting to https. I.e. every such redirect will result in two round trips to the server.
* Filtering happens too late in the request handling process. If you request a resource via http that requires one to be logged in then this issue is picked up first, rather than that the request is coming in via http, so...
  * the user is redirected to the login page (via http),
  * the filter then redirects to https when the login page is requested,
  * the user logs in - however being logged in applies only to the https session,
  * but the stored URI, that the login logic forwards the user to, is the original http request,
  * so as the user is not logged in, as far as http is concerned, they are once again forwarded to a login page (via http)
  * and the cycle repeats until the user gets bored.

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

Currently `cache-control` is set to `no-store` in `web.xml`.

This achieves the desired affect that if the user, after logging out, uses the back button to reach a page for which one needs to be logged in then the user will be redirected to the login page rather than to a still cached copy of the page.

Unfortunately this setting ensures the maximum possible load on the web server. It should be acceptable to allow in-memory caching on the client side as long as the web server is always queried to determine if the cached copy should be viewed as still valid.

However other options such as `no-cache` and `must-revalidate` didn't achieve the desired behavior just described.

There is much conflicting advise regarding this on StackOverflow etc., that `no-store` should be used was determined by experimentation with various options against Chrome 40.

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
