# resources

游戏资源目录，包含音频、字体、着色器和标题等游戏资源。

## 目录结构

### assets

资源根目录。

#### audio

音频资源：

- `accept.mp3` - 确认音效
- `type_in.mp3` - 打字音效

#### fonts

字体资源。

##### Hack-Nerd-Font-Complete-Mono

Hack Nerd Font 等宽字体家族：

- `Hack Bold Italic Nerd Font Complete Mono.ttf`
- `Hack Bold Nerd Font Complete Mono.ttf`
- `Hack Italic Nerd Font Complete Mono.ttf`
- `Hack Regular Nerd Font Complete Mono.ttf`
- `cap2.png` - 字体预览图

##### MapleMono-NF-CN-unhinted

MapleMono NF 中文无 hinting 版本，包含 Regular、Bold、Italic 等多种字重和样式。

#### shaders

GLSL 着色器资源：

- `cursor_frag.glsl` - 光标片段着色器
- `cursor_vert.glsl` - 光标顶点着色器

#### titles

ASCII 艺术标题：

- `ascii_title_chunky.txt` - 粗体 ASCII 标题
- `ascii_title_graffiti.txt` - 涂鸦风格 ASCII 标题
- `ascii_title_rectangles.txt` - 矩形风格 ASCII 标题

## 字体说明

本项目使用以下字体：

| 字体名称        | 用途          | 特点                         |
| --------------- | ------------- | ---------------------------- |
| Hack Nerd Font  | 代码/终端显示 | 等宽字体，Nerd Font 图标支持 |
| MapleMono NF CN | 中文显示      | Maple Mono 中文本地化版本    |

## 音频格式

使用 MP3 格式存储音效，可在 `application.yml` 中配置音频相关参数。

## 着色器

着色器使用 GLSL (OpenGL Shading Language) 编写，用于实现光标渲染等特效。
