# Release Procedure

## Official Releases

### Published Releases

Official listFix() releases can be found [Github releases](https://github.com/Borewit/listFix/releases).

### Versioning

Version policy iis strongly inspired on [Semantic Versioning](https://semver.org/).

Given a version number _MAJOR_._MINOR_._PATCH_, increment the:
1. _MAJOR_:
   - Drastic changes in the application 
2. _MINOR_:
   - Enhancements (new features) without breaking backward compatibility
3. _PATCH_:
   - Bug fixes
   - Update software dependencies
   - Minor software improvements

### Implementation

Versions are assigned using [GitHub annotated tags](https://git-scm.com/book/en/v2/Git-Basics-Tagging), prefixed with a "v".
So version [2.5.1](https://github.com/Borewit/listFix/releases/tag/v2.5.1) is tagged as [`v2.5.1`](https://github.com/Borewit/listFix/releases/tag/v2.5.1).
The "v" prefix helps to semantically distinct version tags from other tags.
Using [`git describe`](https://git-scm.com/docs/git-describe) the build process incorporates the build version into the application.

### Building a release
```shell
./gradlew :clean
./gradlew :packageMyApp
```
Which will result in a corresponding `.exe` and `.msi` and which shall be attached to the GitHub release.
The release will contain a changelog, which will be automatically drafted by the Release-Drafter bot, using [rules defined in release-drafter.yml](../.github/release-drafter.yml).
These rules map [Git labels](https://github.com/Borewit/listFix/labels) assigned to Pull-Requests to following change categories:
- 🚀 Enhancements
- 🎨 Improvements
- 🐛 Bug Fixes
- 🔧 Under the hood

Some labels exclude the Pull-Request from the change-log, like:
- [internal](https://github.com/Borewit/listFix/labels/internal)
- [DevOps](https://github.com/Borewit/listFix/labels/DevOps)

These Pull-Request, did not change the distribution. [internal](https://github.com/Borewit/listFix/labels/internal) 
maybe used to resolve an issue which has not been formally released.

## Informal Releases
Informal releases may be found on Pull-Requests, with versions suffixed with a dask (`-`).
E.g.: `2.5.1-16` or `2.5.1-[PR12]-16`.
The number is the commit distance to the last formal release, the PR annotation is added manually to the installer
to identify the PR it belongs to.

