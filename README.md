# CrestMuse Toolkit (CMX)

CrestMuse Toolkit (CMX) is an open-source programming library for developing music processing sofware.

## Getting Started

### Download Release Artifacts from [GitHub Releases](https://github.com/kitaharalab/cmx/releases)

### Download Snapshot Artifacts from [GitHub Actions](https://github.com/kitaharalab/cmx/actions)

|  | CMX for PC | CMX for Android |
| --- | --- | --- |
| **Workflows** | Java CI | Android CI |

### Build CMX for PC

* Source sets
  * cmx/src/main_common
  * cmx/src/main_pc
* Dependencies
  * cmx/libs
  * cmx/cmx_pc/libs
* Run
  ```
  ./gradlew -p cmx_pc build
  ```
* Artifacts
  * cmx/cmx_pc/build/libs

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