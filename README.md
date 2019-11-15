# A fulcro template

Currently quite under construction.

Based on Aleph as server and Reitit for routing. Includes an example of a UI state machine to handle logging in. Uses [`fulcro-garden-css`](https://github.com/fulcrologic/fulcro-garden-css) to generate CSS.

## Setup

### MacOS
- Get [`Homebrew`](https://brew.sh/)
- Install a JVM runtime, for example `brew tap AdoptOpenJDK/openjdk && brew cask install adoptopenjdk11`.
- `brew install clojure`.
- `brew install yarn`.

## Running and developing

### Running the production version

1. Clone the repo and `cd` into the directory.
2. `yarn` (first time only)
3. `yarn prod`
4. Open a browser to [`localhost:8080`](http://localhost:8080)

### Hacking

1. Clone the repo and `cd` into the directory.
2. `yarn` (first time only)
3. `yarn dev`
4. Connect nREPL to `5555`, or socket REPL to `5556`.
5. If using a Chromium-based browser, get the [`Fulcro Inspect`](https://chrome.google.com/webstore/detail/fulcro-inspect/meeijplnfjcihnhkpanepcaffklobaal) browser extension
6. Open a browser to [`localhost:8080`](http://localhost:8080)

Optional steps:
- `brew install jenv` if you want to run and test on multiple JDK versions and need an easy way to switch between them.
- You can find additional information courtesy of shadow-cljs at [`http://localhost:9630/inspect`](http://localhost:9630/inspect)
