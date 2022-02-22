# CrestMuse Toolkit (CMX)

CrestMuse Toolkit (CMX) is an open-source programming library for developing music processing sofware.

## Getting Started

### Using a published [GitHub Packages](https://github.com/orgs/kitaharalab/packages)

#### In a Gradle project

build.gradle

```
repositories {
    mavenCentral()

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/kitaharalab/cmx")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'cmx:cmx_jre-release:1.1.0'
}

```

Need more instructions? See [cmx-examples](https://github.com/kitaharalab/cmx-examples)

### Download Release Artifacts from [GitHub Releases](https://github.com/kitaharalab/cmx/releases)

### Download Snapshot Artifacts from [GitHub Actions](https://github.com/kitaharalab/cmx/actions)

|  | CMX for JRE | CMX for Android |
| --- | --- | --- |
| **Workflows** | Java CI | Android CI |

### Build CMX for JRE

* Source sets
  * cmx/src/main_common
  * cmx/src/main_jre
* Dependencies
  * cmx/libs
  * cmx/cmx_jre/libs
* Run
  ```
  ./gradlew -p cmx_jre build
  ```
* Artifacts
  * cmx/cmx_jre/build/libs

### Build CMX for Android

* Source sets
  * cmx/src/main_common
  * cmx/src/main_android
* Dependencies
  * cmx/libs
  * cmx/cmx_android/libs
* Run
  ```
  ./gradlew -p cmx_android build
  ```
* Artifacts
  * cmx/cmx_android/build/outputs/aar

### Tutorials

* [Basic usages](tutorials/basic_usages.md)
* [Read/Write MIDI files](tutorials/read_write_midi.md)
* [MusicXML & Deviation]
* [Realtime processing of MIDI input]<!--(tutorials/realtime_processing.md)-->
* [Use MusicRepresentation]<!--(tutorials/music_representation.md)-->
* [Use a Bayesian network built on Weka]<!--(tutorials/bayesian_network.md)-->

## License

This project is licensed under the BSD License - see the [LICENSE.md](LICENSE.md) file for details

## Apps uses CMX

* JamSketch
* JamSketch Android

## Authors

* **Tetsuro Kitahara** (Nihon University, Japan)  
kitahara@kthrlab.jp  
http://www.kthrlab.jp/

* **Junko Fujii**  
fujii@kthrlab.jp

* **Taizan Suzuki** (Picolab Co., LTD)  
http://picolab.jp/

<!-- See also the list of [contributors](contributors.md) who participated in this project. -->