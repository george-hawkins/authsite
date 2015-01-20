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

See https://click.apache.org/docs/user-guide/html/ch06.html

TODO: see how Heroku passes on URLs to web-apps - since https is handled by Heroku and not the web-app.

TODO: include a '<' in a full name and see what happens.
