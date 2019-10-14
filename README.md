# IdeaVim-EasyMotion

Created for [IdeaVim](https://plugins.jetbrains.com/plugin/164-ideavim)  
Powered by [AceJump](https://plugins.jetbrains.com/plugin/7086-acejump)

[EasyMotion](https://github.com/easymotion/vim-easymotion) plugin emulation for IdeaVim.

#### Supported options:

- `g:EasyMotion_re_anywhere`
- `g:EasyMotion_re_line_anywhere`
- `g:EasyMotion_do_mapping`
- `g:EasyMotion_startofline`

#### Supported commands:
Here is a list of commands from EasyMotion documentation.  
Supported commands are marked with `+`  
Unsupported commands are marked with `-`  

```
    <Plug> Mapping Table | Default
    ---------------------|----------------------------------------------
    <Plug>(easymotion-f) | <leader><leader>f{char} +  mapped to fn
    <Plug>(easymotion-F) | <leader><leader>F{char} +  mapped to Fn
    <Plug>(easymotion-t) | <leader><leader>t{char} +  mapped to tn
    <Plug>(easymotion-T) | <leader><leader>T{char} +  mapped to Tn
    <Plug>(easymotion-w) | <leader><leader>w       +
    <Plug>(easymotion-W) | <leader><leader>W       +
    <Plug>(easymotion-b) | <leader><leader>b       +
    <Plug>(easymotion-B) | <leader><leader>B       +
    <Plug>(easymotion-e) | <leader><leader>e       +
    <Plug>(easymotion-E) | <leader><leader>E       +
    <Plug>(easymotion-ge)| <leader><leader>ge      +
    <Plug>(easymotion-gE)| <leader><leader>gE      +
    <Plug>(easymotion-j) | <leader><leader>j       +
    <Plug>(easymotion-k) | <leader><leader>k       +
    <Plug>(easymotion-n) | <leader><leader>n       +
    <Plug>(easymotion-N) | <leader><leader>N       +
    <Plug>(easymotion-s) | <leader><leader>s       +  mapped to sn

    More <Plug> Mapping Table         | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-bd-f)           | See |<Plug>(easymotion-s)|         +
    <Plug>(easymotion-bd-t)           | See |<Plug>(easymotion-bd-t)|      +  mapped to bd-tn
    <Plug>(easymotion-bd-w)           | See |<Plug>(easymotion-bd-w)|      +
    <Plug>(easymotion-bd-W)           | See |<Plug>(easymotion-bd-W)|      +
    <Plug>(easymotion-bd-e)           | See |<Plug>(easymotion-bd-e)|      +
    <Plug>(easymotion-bd-E)           | See |<Plug>(easymotion-bd-E)|      +
    <Plug>(easymotion-bd-jk)          | See |<Plug>(easymotion-bd-jk)|     +
    <Plug>(easymotion-bd-n)           | See |<Plug>(easymotion-bd-n)|      +
    <Plug>(easymotion-jumptoanywhere) | See |<Plug>(easymotion-jumptoanywhere)| +
    <Plug>(easymotion-repeat)         | See |<Plug>(easymotion-repeat)|    -
    <Plug>(easymotion-next)           | See |<Plug>(easymotion-next)|      -
    <Plug>(easymotion-prev)           | See |<Plug>(easymotion-prev)|      -
    <Plug>(easymotion-sol-j)          | See |<Plug>(easymotion-sol-j)|     +
    <Plug>(easymotion-sol-k)          | See |<Plug>(easymotion-sol-k)|     +
    <Plug>(easymotion-eol-j)          | See |<Plug>(easymotion-eol-j)|     +
    <Plug>(easymotion-eol-k)          | See |<Plug>(easymotion-eol-k)|     +
    <Plug>(easymotion-iskeyword-w)    | See |<Plug>(easymotion-iskeyword-w)|    +
    <Plug>(easymotion-iskeyword-b)    | See |<Plug>(easymotion-iskeyword-b)|    +
    <Plug>(easymotion-iskeyword-bd-w) | See |<Plug>(easymotion-iskeyword-bd-w)| +
    <Plug>(easymotion-iskeyword-e)    | See |<Plug>(easymotion-iskeyword-e)|    +
    <Plug>(easymotion-iskeyword-ge)   | See |<Plug>(easymotion-iskeyword-ge)|   +
    <Plug>(easymotion-iskeyword-bd-e) | See |<Plug>(easymotion-iskeyword-bd-e)| +
    <Plug>(easymotion-vim-n)          | See |<Plug>(easymotion-vim-n)|          +
    <Plug>(easymotion-vim-N)          | See |<Plug>(easymotion-vim-N)|          +
                                      |
    Within Line Motion                | See |easymotion-within-line|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-sl)             | See |<Plug>(easymotion-sl)|        +   mapped to sln
    <Plug>(easymotion-fl)             | See |<Plug>(easymotion-fl)|        +   mapped to fln
    <Plug>(easymotion-Fl)             | See |<Plug>(easymotion-Fl)|        +   mapped to Fln
    <Plug>(easymotion-bd-fl)          | See |<Plug>(easymotion-sl)|        +   mapped to sln
    <Plug>(easymotion-tl)             | See |<Plug>(easymotion-tl)|        +   mapped to tln
    <Plug>(easymotion-Tl)             | See |<Plug>(easymotion-Tl)|        +   mapped to Tln
    <Plug>(easymotion-bd-tl)          | See |<Plug>(easymotion-bd-tl)|     +   mapped to bd-tln
    <Plug>(easymotion-wl)             | See |<Plug>(easymotion-wl)|        +
    <Plug>(easymotion-bl)             | See |<Plug>(easymotion-bl)|        +
    <Plug>(easymotion-bd-wl)          | See |<Plug>(easymotion-bd-wl)|     +
    <Plug>(easymotion-el)             | See |<Plug>(easymotion-el)|        +
    <Plug>(easymotion-gel)            | See |<Plug>(easymotion-gel)|       +
    <Plug>(easymotion-bd-el)          | See |<Plug>(easymotion-bd-el)|     +
    <Plug>(easymotion-lineforward)    | See |<Plug>(easymotion-lineforward)|   +
    <Plug>(easymotion-linebackward)   | See |<Plug>(easymotion-linebackward)|  +
    <Plug>(easymotion-lineanywhere)   | See |<Plug>(easymotion-lineanywhere)|  +
                                      |
    Multi Input Find Motion           | See |easymotion-multi-input|
    ----------------------------------|---------------------------------
    <Plug>(easymotion-s2)             | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-f2)             | See |<Plug>(easymotion-f2)|   +  mapped to fn
    <Plug>(easymotion-F2)             | See |<Plug>(easymotion-F2)|   +  mapped to Fn
    <Plug>(easymotion-bd-f2)          | See |<Plug>(easymotion-s2)|   +  mapped to sn
    <Plug>(easymotion-t2)             | See |<Plug>(easymotion-t2)|   +  mapped to tn
    <Plug>(easymotion-T2)             | See |<Plug>(easymotion-T2)|   +  mapped to Tn
    <Plug>(easymotion-bd-t2)          | See |<Plug>(easymotion-bd-t2)|  +  mapped to bd-tn
                                      |
    <Plug>(easymotion-sl2)            | See |<Plug>(easymotion-sl2)|    +   mapped to sln
    <Plug>(easymotion-fl2)            | See |<Plug>(easymotion-fl2)|    +   mapped to fln
    <Plug>(easymotion-Fl2)            | See |<Plug>(easymotion-Fl2)|    +   mapped to Fln
    <Plug>(easymotion-tl2)            | See |<Plug>(easymotion-tl2)|    +   mapped to tln
    <Plug>(easymotion-Tl2)            | See |<Plug>(easymotion-Tl2)|    +   mapped to Tl2
                                      |
    <Plug>(easymotion-sn)             | See |<Plug>(easymotion-sn)|     +
    <Plug>(easymotion-fn)             | See |<Plug>(easymotion-fn)|     +
    <Plug>(easymotion-Fn)             | See |<Plug>(easymotion-Fn)|     +
    <Plug>(easymotion-bd-fn)          | See |<Plug>(easymotion-sn)|     +
    <Plug>(easymotion-tn)             | See |<Plug>(easymotion-tn)|     +
    <Plug>(easymotion-Tn)             | See |<Plug>(easymotion-Tn)|     +
    <Plug>(easymotion-bd-tn)          | See |<Plug>(easymotion-bd-tn)|  +
                                      |
    <Plug>(easymotion-sln)            | See |<Plug>(easymotion-sln)|    +
    <Plug>(easymotion-fln)            | See |<Plug>(easymotion-fln)|    +
    <Plug>(easymotion-Fln)            | See |<Plug>(easymotion-Fln)|    +
    <Plug>(easymotion-bd-fln)         | See |<Plug>(easymotion-sln)|    +
    <Plug>(easymotion-tln)            | See |<Plug>(easymotion-tln)|    +
    <Plug>(easymotion-Tln)            | See |<Plug>(easymotion-Tln)|    +
    <Plug>(easymotion-bd-tln)         | See |<Plug>(easymotion-bd-tln)| +

    Over Window Motion                | (No assignment by default)
    ----------------------------------|---------------------------------
    <Plug>(easymotion-overwin-f)      | See |<Plug>(easymotion-overwin-f)|      -
    <Plug>(easymotion-overwin-f2)     | See |<Plug>(easymotion-overwin-f2)|     -
    <Plug>(easymotion-overwin-line)   | See |<Plug>(easymotion-overwin-line)|   -
    <Plug>(easymotion-overwin-w)      | See |<Plug>(easymotion-overwin-w)|      -
```

