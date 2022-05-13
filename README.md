# Sample QR Code Reader

Sample app showcasing how to implement QR Code Reading in Android using CameraX and 2 different libs:
- [Google ML Kit - Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- [ZXing - Zebra Crossing](https://github.com/zxing/zxing)

## APK Sizes
It's worth mentioning that the final apk sizes using ZXing OR ML Kit are very different:
- Google ML Kit
  - debug: 14.1 MB
  - release: 12.5 MB
- ZXing
  - debug: 5.2 MB
  - release: 4.2 MB
- Just CameraX (running preview only) for comparison
  - debug: 4.9 MB
  - release: 4 MB

## Git repository structure
There are 2 main branches in the repo:
- `mlkit`: branch with the ML Kit implementation in `:app` module
- `zxing`: branch with the ZXing implementation in `:app` module


## References
The implementation here was based on two posts:
- [How to Create a QR Code Scanner App in Android](https://learntodroid.com/how-to-create-a-qr-code-scanner-app-in-android/)
- [Using Google’s MLKit and CameraX for Lightweight Barcode Scanning](https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc)
