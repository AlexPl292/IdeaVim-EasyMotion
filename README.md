# IdeaVim-EasyMotion

[![][jetbrains-team-svg]][jetbrains-team-page]
[![][apache-license-svg]](LICENSE)

Created for [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim)  
Powered by [AceJump](https://plugins.jetbrains.com/plugin/7086-acejump)

[EasyMotion](https://github.com/easymotion/vim-easymotion) plugin emulation for IdeaVim.


#### Setup

- Install [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim),
[AceJump](https://plugins.jetbrains.com/plugin/7086-acejump) and
[IdeaVim-EasyMotion](https://plugins.jetbrains.com/plugin/13360-ideavim-easymotion/) plugins.
- Add `set easymotion` to your `~/.ideavimrc`

#### How does it work?

Please check the docs of the [vim-easymotion](https://github.com/easymotion/vim-easymotion#usage-example-for-the-base-features) plugin.

#### What are the features of this new plugin and how does this plugin differ from AceJump?

- With IdeaVim-EasyMotion, you get good integration with IdeaVim.
    - Now you can use EasyMotion commands as an argument for `d`, `c`, or any other command that takes motion as an argument.
    - Use your existing EasyMotion mappings or create new mappings that will work both in Vim and IdeaVim.
    - If you use the `iskeyword` option, check out the ` <Plug>(easymotion-iskeyword-*)` commands.
- There is now an additional set of commands. Use `<Plug>(easymotion-e)` to jump to a word and, or `<Plug>(easymotion-sl)` for jumping within the line. See the [full list of supported commands](#supported-commands).

#### Keep typing

You don’t have to limit yourself to using only one char of the target word. Type as many chars as you need.

#### AceJump mapping

For it to work properly, the mappings of IdeaVim-EasyMotion should be
executed instead of AceJump mappings. This plugin maps the shortcuts
of AceJump to the corresponding actions in IdeaVim-EasyMotion to
improve the experience of AceJump users. You can disable this feature by
adding `let g:EasyMotion_override_acejump = 0` to your `~/.ideavimrc`.

An additional feature of this plugin: AceJump jumps will be added to IdeaVim jump list (no need for `set easymotion`).

#### `mapleader` mapping

If you want to change your leader key, please make sure the `mapleader` command appears before the `set easymotion` command in your `~/.ideavimrc`:
```
let mapleader=","
set easymotion
```

#### Supported options:

- `g:EasyMotion_re_anywhere`
- `g:EasyMotion_re_line_anywhere`
- `g:EasyMotion_do_mapping`
- `g:EasyMotion_startofline`

#### Supported commands:
80 of the original 87 vim-easymotion commands are supported.  
[Here](https://github.com/easymotion/vim-easymotion/blob/master/doc/easymotion.txt) you can get
the description of each `<Plug>` command.

  `<ll>` means that you have to prefix the command with `<leader><leader>`.
  So, type `<leader><leader>w` to execute `<ll>w` command.
```

   Default Mapping |  <Plug> command       |
   -----------------------------------------------------------------
    <ll>f{char}    |  <Plug>(easymotion-f) |  mapped to fn
    <ll>F{char}    |  <Plug>(easymotion-F) |  mapped to Fn
    <ll>t{char}    |  <Plug>(easymotion-t) |  mapped to tn
    <ll>T{char}    |  <Plug>(easymotion-T) |  mapped to Tn

    <ll>w          |  <Plug>(easymotion-w) |
    <ll>W          |  <Plug>(easymotion-W) |
    <ll>b          |  <Plug>(easymotion-b) |
    <ll>B          |  <Plug>(easymotion-B) |
    <ll>e          |  <Plug>(easymotion-e) |
    <ll>E          |  <Plug>(easymotion-E) |
    <ll>ge         |  <Plug>(easymotion-ge |
    <ll>gE         |  <Plug>(easymotion-gE |
    <ll>j          |  <Plug>(easymotion-j) |
    <ll>k          |  <Plug>(easymotion-k) |
    <ll>n          |  <Plug>(easymotion-n) |
    <ll>N          |  <Plug>(easymotion-N) |
    <ll>s          |  <Plug>(easymotion-s) |  mapped to sn


    More <Plug> Mapping Table         |  Note
    ----------------------------------|----------------------
    <Plug>(easymotion-bd-f)           |
    <Plug>(easymotion-bd-t)           |  mapped to bd-tn
    <Plug>(easymotion-bd-w)           |
    <Plug>(easymotion-bd-W)           |
    <Plug>(easymotion-bd-e)           |
    <Plug>(easymotion-bd-E)           |
    <Plug>(easymotion-bd-jk)          |
    <Plug>(easymotion-bd-n)           |
    <Plug>(easymotion-jumptoanywhere) |

    <Plug>(easymotion-repeat)         |  UNSUPPORTED
    <Plug>(easymotion-next)           |  UNSUPPORTED
    <Plug>(easymotion-prev)           |  UNSUPPORTED

    <Plug>(easymotion-sol-j)          |
    <Plug>(easymotion-sol-k)          |
    <Plug>(easymotion-eol-j)          |
    <Plug>(easymotion-eol-k)          |
    <Plug>(easymotion-iskeyword-w)    |
    <Plug>(easymotion-iskeyword-b)    |
    <Plug>(easymotion-iskeyword-bd-w) |
    <Plug>(easymotion-iskeyword-e)    |
    <Plug>(easymotion-iskeyword-ge)   |
    <Plug>(easymotion-iskeyword-bd-e) |
    <Plug>(easymotion-vim-n)          |
    <Plug>(easymotion-vim-N)          |

    Within Line Motion                | Note 
    ----------------------------------|---------------------------------
    <Plug>(easymotion-sl)             |  mapped to sln
    <Plug>(easymotion-fl)             |  mapped to fln
    <Plug>(easymotion-Fl)             |  mapped to Fln
    <Plug>(easymotion-bd-fl)          |  mapped to sln
    <Plug>(easymotion-tl)             |  mapped to tln
    <Plug>(easymotion-Tl)             |  mapped to Tln
    <Plug>(easymotion-bd-tl)          |  mapped to bd-tln

    <Plug>(easymotion-wl)             | 
    <Plug>(easymotion-bl)             | 
    <Plug>(easymotion-bd-wl)          | 
    <Plug>(easymotion-el)             | 
    <Plug>(easymotion-gel)            | 
    <Plug>(easymotion-bd-el)          | 
    <Plug>(easymotion-lineforward)    |
    <Plug>(easymotion-linebackward)   |
    <Plug>(easymotion-lineanywhere)   |
                                      
    Multi Input Find Motion           | Note
    ----------------------------------|---------------------------------
    <Plug>(easymotion-s2)             |  mapped to sn
    <Plug>(easymotion-f2)             |  mapped to fn
    <Plug>(easymotion-F2)             |  mapped to Fn
    <Plug>(easymotion-bd-f2)          |  mapped to sn
    <Plug>(easymotion-t2)             |  mapped to tn
    <Plug>(easymotion-T2)             |  mapped to Tn
    <Plug>(easymotion-bd-t2)          |  mapped to bd-tn
                                      |
    <Plug>(easymotion-sl2)            |  mapped to sln
    <Plug>(easymotion-fl2)            |  mapped to fln
    <Plug>(easymotion-Fl2)            |  mapped to Fln
    <Plug>(easymotion-tl2)            |  mapped to tln
    <Plug>(easymotion-Tl2)            |  mapped to Tl2
                                      |
    <Plug>(easymotion-sn)             | 
    <Plug>(easymotion-fn)             | 
    <Plug>(easymotion-Fn)             | 
    <Plug>(easymotion-bd-fn)          | 
    <Plug>(easymotion-tn)             | 
    <Plug>(easymotion-Tn)             | 
    <Plug>(easymotion-bd-tn)          | 
                                      |
    <Plug>(easymotion-sln)            | 
    <Plug>(easymotion-fln)            | 
    <Plug>(easymotion-Fln)            | 
    <Plug>(easymotion-bd-fln)         | 
    <Plug>(easymotion-tln)            | 
    <Plug>(easymotion-Tln)            | 
    <Plug>(easymotion-bd-tln)         | 

    Over Window Motion                | Note
    ----------------------------------|---------------------------------
    <Plug>(easymotion-overwin-f)      | UNSUPPORTED
    <Plug>(easymotion-overwin-f2)     | UNSUPPORTED
    <Plug>(easymotion-overwin-line)   | UNSUPPORTED
    <Plug>(easymotion-overwin-w)      | UNSUPPORTED


    Doesn't exist in EasyMotion       | Description
    ----------------------------------|----------------------------------
    <Plug>(acejump-linemarks)         | Analog of Line Motion in AceJump
```

<!-- Badges -->
[jetbrains-team-page]: https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub
[jetbrains-team-svg]: http://jb.gg/badges/team.svg
[plugin-download-svg]: https://img.shields.io/jetbrains/plugin/d/7086-acejump.svg
[apache-license-svg]: https://img.shields.io/badge/License-GPL%20v3-blue.svg
