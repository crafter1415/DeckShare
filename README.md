# DeckShare

Puzzlineのデッキをデッキコード化して共有するためだけのプログラム  
[公式コメント](https://discord.com/channels/1037186820145958972/1101315382008098826/1128596172219613245)
よりしばらくデッキコードが実装されなさそうだったため自分用に作成  
平均600字・最長1500字のクソ長コードでも許せる人向け

for Puzzline 1.6.x

## ビルド手順

1. JDK17を用意する
   - 任意だがOpenJFX17も用意してclasspathに指定しておくと成果物をダブルクリックで実行可能になる
2. 本リポジトリを複製する
3. リポジトリのルートディレクトリでコンソールを開き以下を実行する
   ```shell
   mvnw clean package
   ```
4. `target` 下に生成された `DeckShare-<version>.jar` を適当な場所にコピーして利用する

## 使用方法

必要な環境: JRE17

### CLIとして利用する場合

使用方法は `java -jar DeckShare.jar --help` をコマンドライン上で実行することで確認可能

### GUIとして利用する場合

別途 OpenJFX をクラスパスに追加する必要がある (追加方法は各自確認)  
環境構築後はjarファイルダブルクリックで実行可能

## 使用ライブラリ

[OpenJFX](https://openjfx.io/)
[Gson](https://github.com/google/gson)