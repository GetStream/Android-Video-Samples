## 💻 How to build the project?

For proper project setup for the sample projects ([Video Calling Sample](https://github.com/GetStream/Android-Video-Samples/tree/main/video-call-sample/), [Audio Room Sample](https://github.com/GetStream/Android-Video-Samples/tree/main/audio-room-sample/), and [Livestreaming Sample](https://github.com/GetStream/Android-Video-Samples/tree/main/livestreaming-sample/)) it's essential to follow the instructions below. In most cases, you can complete all configuration steps within 10 to 20 minutes:

1. Go to the __[Stream login page](https://getstream.io/try-for-free?utm_source=Github&utm_medium=Jaewoong_OSS&utm_content=Developer&utm_campaign=Github_Mar2024_Jaewoong_Android_Samples&utm_term=DevRelOss)__.

2. If you have your GitHub or Google account, click the **Continue with GitHub** or **Continue with Google** button and you can sign up within a couple of seconds. 

![stream](https://github.com/skydoves/chatgpt-android/raw/main/figures/stream0.png)

3. Go to the __[Dashboard](https://dashboard.getstream.io?utm_source=Github&utm_medium=Jaewoong_OSS&utm_content=Developer&utm_campaign=Github_Mar2024_Jaewoong_Android_Samples&utm_term=DevRelOss)__ and click the **Create App** button like the below.

![stream](https://github.com/skydoves/chatgpt-android/raw/main/figures/stream1.png)

4. Fill in the blanks like the below and click the **Create App** button.

![stream](https://github.com/skydoves/chatgpt-android/raw/main/figures/stream2.png)

5. You will see the **Key** like the figure below and then copy it.

![stream](https://github.com/skydoves/chatgpt-android/raw/main/figures/stream3.png)

6. Create a new file named **secrets.properties** on the root directory of this Android project, and add the key to the `secrets.properties` file like the below:

![stream](https://raw.githubusercontent.com/skydoves/gemini-android/b70c8c13476e84a09014b5efb206a07049993a4f/figures/stream5.png)

```gradle
STREAM_API_KEY=..
```

7. Go to your __[Dashboard](https://dashboard.getstream.io?utm_source=Github&utm_medium=Jaewoong_OSS&utm_content=Developer&utm_campaign=Github_Mar2024_Jaewoong_Android_Samples&utm_term=DevRelOss)__ again and click your App.

9. In the **Overview** menu, you can find the **Authentication** category by scrolling to the middle of the page.
10. Switch on the **Disable Auth Checks** option and click the **Submit** button like the figure below.
11. Run and build your project! 🚀
## License

```
The MIT License (MIT)

Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```