# Passwordless authentication in Scala

*June 19, 2014*

Adding password authentication to a system is a pain in the neck, both for 
users and for developers.  It forces users to think of and remember yet another 
set of credentials, and requires developers to build secure mechanisms for the 
establishment and management of credentials.  In the end, these credentials are 
no more secure than a user's own email account, where the inevitable *forgot 
password* email will be sent.

One way around this is to forego the password entirely, and go directly to 
email-based authentication.  In this example, we build a Servlet filter that 
we can add to a Scala Web application to protect it in this way.

First, lets identify the different stages of authentication, and in which order 
we want our filter to handle them.

1. User requests to sign out
1. User requests a new authentication key
1. User signs in with an unused authentication key
1. User is authenticated with an established session key
1. Unauthenticated user accesses unrestricted content

Whatever else a user might be up to, if they tell us the want to sign out, we 
invalidate their session, and trash their locally cached credentials:

```scala
val req: HttpServletRequest = ...
val res: HttpServletResponse = ...

findSignout(req) map { _ =>
  val c = new Cookie("session_key", null)
  res.addCookie(c)
  res.sendRedirect(req.getRequestURI)
}
```

If the user is not attempting to sign out, and they want us to send them a new 
one-time authentication key, we email them a link with the key included, and 
respond with a page telling them so:

```scala
orElse findEmail(req).map { e =>
  val nonce = DB.addNonce(Email(e))
  val url = req.getRequestURL + "?auth_key=" + nonce.value
  val message = "sign-in at " + url
  email(e, "xwp-auth@local", "your sign-in url", message)
  val body =
    <html>
      <body>
        <h1>email sent!</h1>
      </body>
    </html>
  res.getWriter.write(body.toString)
 }
```

When the user later clicks that link, we invalidate their one-time 
authentication key and create a longer-lived session they can use for future 
authentication:

```scala
orElse findNonce(req).map { e =>
  val s = DB.addSession(e)
  setSession(s)(res)
  res.sendRedirect(req.getRequestURI)
}
```

For subsequent interaction, we find and validate the session provided by the 
user, and continue to the rest of the Servlet stack:

```scala
orElse findSession(req).map { x =>
  fc.doFilter(req, res)
}
```

Finally, if none of the above applies, the user is unauthenticated and we show 
them the unrestricted version of our application:

```scala
orElse {
  val body =
    <html>
      <body>
        <h1>welcome, visitor</h1>
        <form method="post"> 
          <input type="text" name="auth_email" placeholder="email address" />
          <input type="submit" value="sign in" />
        </form>
      </body>
    </html>
  res.getWriter.write(body.toString)
  None
}
```
