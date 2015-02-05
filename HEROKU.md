The `master` branch can be pushed directly to Heroku.

However this branch uses Jekyll to build the website and as Jekyll sites cannot easily be built on Heroku when using the Java build pack, we need to build it on a local branch and push the result to Heroku.

This can be done by creating a local branch called `heroku` and using the `heroku-push` script as described here.

Setup
-----

```bash
$ git checkout -b heroku
```

Setup SSL if required as outlined in the [README](https://github.com/george-hawkins/authsite/tree/jekyll#ssl).

Add Heroku as a remote - the appropriate URL can be found on the dashboard.heroku.com settings page for the app.

```bash
$ git remote add heroku git@heroku.com:<app-name>.git
```

Return to your original branch.

```bash
$ git checkout <my-branch>
```

Now whenever you make updates you can build the Jekyll site and push the changes to Heroku like so:

```bash
$ heroku-push
```

The `heroku-push` command always first merges any changes on the current branch to the `heroku` branch.
