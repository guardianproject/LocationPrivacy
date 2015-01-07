
This is an app that is meant to be a transparent filter for all of the various
ways of sharing location.  It accepts insecure methods of sharing location,
and then converts them to more secure methods.  This mostly means that it
rewrites URLs to use https, and even to use `geo:` URIs, which can work on
fully offline setups.

This was started as part of the T2 Panic work, since sharing location is so
often a part of panic apps.

https://dev.guardianproject.info/projects/panic
