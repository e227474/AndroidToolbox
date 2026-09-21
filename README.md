<p align="center">
<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp">
</p>
<h1 align="center">AndroidToolbox</h1>

Providing functionality missing in the [more secure](https://grapheneos.org/features) AOSP-bases distributions (Android™, GrapheneOS) such as curl compared to less secure "desktop" operating systems. 

## Features

- [x] Basic Curl implementation for fetching HTML, JavaScript, CSS, Markdown and Other (treated as txt) from a URL and displaying it using the system WebView.)
 - [x] "Save as" FAB dialog allowing to:
  - save as Plaintext txt
  - save as source
  - share
- [X] Date difference Calculator
- [x] MDY UI, both in native Compose and WebView parts of the app with large screen support.
- [x] Disabled insecure JIT compilation
- [x] opt into highest security available MTE modes

## Screenshots
<p align="center">
<img height="512" src="https://raw.githubusercontent.com/e227474/AndroidToolbox/refs/heads/master/app_screenshot-pixel10profold.png">
</p> 

## Backlog
- [ ] publish app on [Accrescent](https://accrescent.app) once [submissions are public](https://infosec.exchange/@accrescent/117152213429980044)

## Permissions
- `android.permission.INTERNET` (required for curl functionality)
- [sensors permission](https://grapheneos.org/features#sensors-permission-toggle) (only on [GrapheneOS](https://grapheneos.org), can't be removed as a developer) 

## Architecture

```mermaid
---
  layout: dagre
---
graph TD
    %% Style Definitions
    classDef uiLayer fill:#e1f5fe,stroke:#01579b,stroke-width:2px,color:#01579b;
    classDef logicLayer fill:#f1f8e9,stroke:#33691e,stroke-width:2px,color:#33691e;
    classDef externalLayer fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#e65100;

    subgraph UI_Layer [UI Layer - Jetpack Compose]
        MainScreen([Main Screen])
        DateCalcUI([Date Difference Calculator])
        WebViewContainer([WebView Display])
        SaveDialog([Save As / Share Dialog])
    end

    subgraph Logic_Layer [Logic & Service Layer]
        CurlEngine([Curl Network Client])
        DateLogic([Date Calculation Logic])
        FileHandler([File & Share Manager])
    end

    subgraph External_Layer [External / System Layer]
        Internet((Internet))
        AndroidStorage[(Android Storage)]
        AndroidIntents([Android Share Intents])
    end

    %% User Interactions
    MainScreen --> DateCalcUI
    MainScreen --> WebViewContainer
    WebViewContainer --> SaveDialog

    %% Data Flows
    WebViewContainer --> CurlEngine
    CurlEngine <--> Internet
    CurlEngine --> WebViewContainer

    DateCalcUI <--> DateLogic

    SaveDialog --> FileHandler
    FileHandler --> AndroidStorage
    FileHandler --> AndroidIntents

    %% Assigning Styles
    class MainScreen,DateCalcUI,WebViewContainer,SaveDialog uiLayer;
    class CurlEngine,DateLogic,FileHandler logicLayer;
    class Internet,AndroidStorage,AndroidIntents externalLayer;
```

## Paper
(will be added once finished with personally identifying information redacted)
