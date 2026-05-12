# React Native Monterosa SDK

Monterosa / Interaction SDK allows you to embed an Experience in your React Native app.

## Getting up & running with the code

In order to get up & running with the code, execute the following commands:

```
yarn clean
yarn bootstrap
yarn build:android
yarn build:ios
```

When all those commands succeed, you should be able to run the app.

### Code structure

The main folders of the app are:

* src/ - The JS source code of the library.
* android/ - The Android native code of the library. Receives the config for the view, and creates and manages it for RN.
* ios/ - The Android native code of the library. Receives the config for the view, and creates and manages it for RN.
* example/ - An example app to check all works correctly
  * example/src - The RN client code using the SDK. Add here usages of any function added to the SDK.
  * example/android - An Android project that is able to be executed from Android Studio. Will execute the code in example/src. This allows debugging.
  * example/ios - An iOS project that is able to be executed from Xcode. Will execute the code in example/src. This allows debugging.

## Run the app

You can run the app using:

```
yarn example start
```

an interactive prompt will appear that lets you press `i` to launch iOS or `a` to launch Android.

## Releasing the app

In order to release the app, go to https://gitlab.com/groups/monterosa-sdk/-/settings/repository and generate a deploy token with write permissions for registry and package registry.

Once done, copy the deploy token value (not the username) and execute:

```
export NPM_TOKEN=<MY TOKEN>
```

That's all the environment setup needed. After that use the appropriate pre-release command depending on what you need:

| Command                | Example result                | Use when                              |
|------------------------|-------------------------------|---------------------------------------|
| `yarn prereleaseRC`    | `0.3.0-rc.0` → `0.3.0-rc.1`  | Incrementing an existing RC           |
| `yarn prereleasePatch` | `0.3.0` → `0.3.1-rc.0`       | Starting an RC for a new patch release |
| `yarn prereleaseMinor` | `0.3.0` → `0.4.0-rc.0`       | Starting an RC for a new minor release |
| `yarn prereleaseMajor` | `0.3.0` → `1.0.0-rc.0`       | Starting an RC for a new major release |

This will create a new commit, tag and release of the SDK that won't be yet available by default to users (they can opt-in though).

```
Joseps-MacBook-Pro:react-native josep$ yarn prereleaseMinor
yarn run v1.22.21
$ release-it --increment preminor --preRelease rc --ci
(node:77147) [DEP0040] DeprecationWarning: The `punycode` module is deprecated. Please use a userland alternative instead.
(Use `node --trace-deprecation ...` to show where the warning was created)
WARNING The recommended bump is "minor", but is overridden with "preminor".
🚀 Let's release @monterosa-sdk/react-native (0.1.1...0.2.0-rc.0)
Changelog:
* Merge branch 'feature/parameters' into 'main' (2f65181)
* feat: added parameter input (3e81ee8)
✔ npm version
Changeset:
 M package.json
✔ npm publish
✔ Git commit
✔ Git tag
✔ Git push
🔗 https://gitlab.com/api/v4/projects/38419920/packages/npm/package/@monterosa-sdk/react-native
🏁 Done (in 16s.)
✨  Done in 17.77s.
```

Once you have passed testing and are happy with the SDK, please do the final release with either `yarn releasePatch`, `yarn releaseMinor` or `yarn releaseMajor`. The behaviour will be equally hands-free to creating a prerelease.

**NOTE**: This can be executed from any branch. Make sure the final release tag is placed on the `main` branch for consistency with git flow.
