# Change Log
All notable changes to this project, starting from 0.5.0, will be documented in this file.

## [0.5.1]
### Added
- Client: dbg system
- Client: send-msg, replaces app-send
- Server: uuid DB
- Server: new clean send-msg (mirrors client)

### Changed
- Moved to Reagent 0.8.1

### Fixed
- Client: opts on registration was not xform'ed

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


[0.6.0]: https://github.com/your-name/hanami/compare/0.5.1...0.6.0
[0.5.1]: https://github.com/your-name/hanami/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/your-name/hanami/compare/0.4.0...0.5.0
