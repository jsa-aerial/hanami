# Change Log
All notable changes to this project, starting from 0.5.0, will be documented in this file.

## [0.8.1]
### Added
- Client: Support for user/app specific symbol translate on hiccup vecs


## [0.8.0]
### Added
- Client: `hmi/sv!` as mirror of server side `hmi/sv!`. Client only apps support
- Client: `init-tabs` refactor `register`. Client only apps support
- Client: `frameit` refactor `vis-list`. Client only apps support
- Common: `:scaleFactor` to `:vgl` value in `hc/default-opts`. Support Vega-Embed export scaling
- Client: `make-frame` refactor instrumentor-fn handling.
- Client: `make-instrumentor` refactor making instrumentor - client only apps support
- Common: Add `:sort` support for default xy-encoding
- Common: Add tooltip title and format support for default-tooltip
- Common: Add generalized substitution keys for :XTTIP and :YTTIP
- Client: All frames now have an HTML id" (for getElementById)
- Client: Support for frame callbacks

### Fixed
- main rgt/render [hanami-main] did not use passed in element!

### Changed
- Client: internal name change user-dispatch to message-dispatch
- Server: internal name change server-dispatch to message-dispatch

### Deprecated


## [0.6.0]
### Added
- Client: dbg system
- Client: send-msg, replaces app-send
- Server: uuid DB
- Server: new clean send-msg (mirrors client)

### Changed
- Moved to Reagent 0.8.1

### Fixed
- Client: opts on registration was not xform'ed
- Client: remove bogus extra refs from re-com.box refer. Fixes shadow-cljs

### Deprecated
- Client: app-send, use send-msg


## [0.5.1]
### Added
- Template for over plus detail charts `ht/overview-detail. And new substitution key :DHEIGHT for controlling height of overview area. Experimental - may not work for all chart types
- :MODE "vega-lite" and :RENDERER "canvas" default substitution keys
- :XAXIS and :YAXIS substitution keys

### Changed
- x & y encoding :axis are now :XAXIS and :YAXIS. This is more general
- (default-opts :vgl) now uses :MODE and :RENDERER instead of hardcoded values

## [0.5.0]
### Added
- Chartless Picture frames (frames are hiccup and re-com enabled top, bottom, left, right areas surrounding a vis) Provides simple and easy support for full text based areas in tabs (chapters)
- New file data sources (previously only supported `values`, `url`, and `named` channels)
- New templates and default substitution keys to support tree and network layouts
- Stronger and more general support for adding application specific routes and landing page to Hanami websocket routes
- Nifty debug print system


[0.8.1]: https://github.com/your-name/hanami/compare/0.8.0...0.8.1
[0.8.0]: https://github.com/your-name/hanami/compare/0.6.0...0.8.0
[0.6.0]: https://github.com/your-name/hanami/compare/0.5.1...0.6.0
[0.5.1]: https://github.com/your-name/hanami/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/your-name/hanami/compare/0.4.0...0.5.0
