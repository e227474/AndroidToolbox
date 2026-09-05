<p align="center">
<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp">
</p>
<h1 align="center">AndroidToolbox</h1>

Providing functionality missing in the [more secure](https://grapheneos.org/features) AOSP-bases distributions (Android™, GrapheneOS) such as curl compared to less secure "desktop" operating systems. 

## Features

- [x] Basic Curl implementation (fetching HTML from a URL and displaying it using the system WebView. 
- [x] MDY UI, both in native Compose and WebView parts of the app.
- [x] Disables insecure JIT compilation
- [x] opt into highest security available MTE modes
- [x] "Save as" FAB dialog allowing to:
  - save as Plaintext txt
  - save as HTML
  - share

- [ ] ~~open dev tools for a provided URL (under consideration)~~ (Not possible without major workarounds)
- [ ] automatically detect media urls inside fetched HTML and provide options to download them (under consideration)
- [ ] publish app on [Accrescent](https://accrescent.app) once [submissions are public](https://infosec.exchange/@accrescent/117152213429980044)
