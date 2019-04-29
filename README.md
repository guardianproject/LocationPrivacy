
LocationPrivacy is not really app but rather a set of "Intent Filters" for all
of the various ways of sharing location.  When you share location from one
app, LocationPrivacy offers itself as an option.  It then recognizes insecure
methods of sharing location, and then converts them to more secure methods.
This mostly means that it rewrites URLs to use https, and even to use `geo:`
URIs, which can work on fully offline setups.  LocationPrivacy mostly works by
reading the location information from the URL itself.  For many URLs,
LocationPrivacy must actually load some of the webpage in order to get the
location.

LocationPrivacy can also serve as a way to redirect all location links to your
favorite mapping app.  All map apps in Android can view `geo:` URIs, and
LocationPrivacy converts many kinds of links to `geo:` URIs, including: Google
Maps, OpenStreetMap, Amap, Baidu Map, QQ Map, Nokia HERE, Yandex Maps.

This was started as part of the T2 Panic work, since sharing location is so
often a part of panic apps.  Follow our progress here:

* https://guardianproject.info/tag/panic
* https://dev.guardianproject.info/projects/panic


Licenses
========

The app itself it released under the GNU GPLv3+.  Here are some specific
credits:

* the app icon is public domain via CC0 license:
  http://pixabay.com/en/map-compass-travel-navigation-27617/

* the feature graphic is a self-modified version of public domain file:
  https://commons.wikimedia.org/wiki/File:NASA-Apollo8-Dec24-Earthrise.jpg
