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
* ios/ - The iOS native code of the library. Receives the config for the view, and creates and manages it for RN.
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

## Testing

```
yarn test
```

This runs Jest tests for the JS/TS layer. Android (Kotlin JUnit) and iOS (XCTest) tests run automatically in CI via the `test-unit` Bitrise workflow on every push to `develop` and all PRs.

## Releasing

All releases publish to the [GitLab npm registry](https://gitlab.com/groups/monterosa-sdk/-/packages/) only — nothing goes to public npm.

### Setup (one-time)

Go to https://gitlab.com/groups/monterosa-sdk/-/settings/repository and generate a deploy token with write permissions for registry and package registry. Then:

```
export NPM_TOKEN=<YOUR_TOKEN>
```

### Prerelease (RC)

Use the `prerelease:*` commands to create or increment a release candidate:

| Command | From | Result | Use when |
|---|---|---|---|
| `yarn prerelease:patch` | `0.3.1` | `0.3.2-rc.0` | Starting an RC for a patch |
| `yarn prerelease:patch` | `0.3.2-rc.0` | `0.3.2-rc.1` | Incrementing an existing RC |
| `yarn prerelease:minor` | `0.3.1` | `0.4.0-rc.0` | Starting an RC for a minor |
| `yarn prerelease:major` | `0.3.1` | `1.0.0-rc.0` | Starting an RC for a major |

These commands are smart — if you're already on an RC, they increment the RC number instead of bumping the version again.

### Final release

Once testing is complete, promote the RC to a final release:

```
yarn release
```

This strips the `-rc.X` suffix (e.g. `0.3.2-rc.3` → `0.3.2`). It will fail if the current version is not an RC.

### Releasing from CI (Bitrise)

The same release commands are available as Bitrise workflows — no need to run locally:

| Workflow | How to trigger | Env var | What it does |
|---|---|---|---|
| `release-prerelease` | Manual start in Bitrise | `RELEASE_LEVEL=patch/minor/major` | Creates/increments RC |
| `release-final` | Manual start in Bitrise | — | Promotes RC to final |

Both workflows verify the publish target is GitLab before proceeding.

**Bitrise secret required:** `NPM_TOKEN` (GitLab deploy token with `write_registry` scope).

### Legacy commands (deprecated)

These still work for backwards compatibility but the `prerelease:*` / `release` commands above are preferred:

| Command | Behaviour |
|---|---|
| `yarn prereleasePatch` | Always runs `prepatch` (does NOT auto-detect RC) |
| `yarn prereleaseMinor` | Always runs `preminor` (does NOT auto-detect RC) |
| `yarn prereleaseMajor` | Always runs `premajor` (does NOT auto-detect RC) |
| `yarn prereleaseRC` | Increments RC number only |
| `yarn releasePatch` | Bumps patch directly (no RC guard) |
| `yarn releaseMinor` | Bumps minor directly (no RC guard) |
| `yarn releaseMajor` | Bumps major directly (no RC guard) |

**NOTE**: Releases can be executed from any branch. Make sure the final release tag is placed on the `main` branch for consistency with git flow.

## CI Workflows (Bitrise)

| Workflow | Trigger | Description |
|---|---|---|
| `test-unit` | Auto: push to `develop`, all PRs | JS (Jest) + Android (Kotlin JUnit) + iOS (XCTest) |
| `release-prerelease` | Manual | Create/increment RC, publish to GitLab |
| `release-final` | Manual | Promote RC to final, publish to GitLab |
| `deploy-example-ios` | Manual | Build example app, deploy to TestFlight |
| `deploy-example-android` | Manual | Build example app, deploy to Firebase App Distribution |
