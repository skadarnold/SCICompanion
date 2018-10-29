# SCICompanion
SCI Companion - a complete IDE for Sierra SCI games (SCI0 to SCI1.1)

Official website:
http://scicompanion.com

General notes:
The bulk of the code is in SCICompanionLib\Src

SCICompanion is the .exe which is just a thin wrapper over SCICompanionLib

## A note from Kawa
The following defines are available:

* `PHIL_EXISTS` - Enables the use of the `&exists` keyword, as in `(if (&exists theX) ...)` instead of `(if (>= argc 1) ...)`.
* `PHIL_LDMSTM` - Enables variable dereferencing (`*var`) as an alternative to `(Memory memPEEK)` and `(Memory memPOKE)`. Requires a special build of the SCI terp with two new opcodes. SCI16+ and Phil's have it.
* `PHIL_FOREACH` - Enables the use of the `foreach` keyword. `(foreach val anArray ...)` or `(foreach val aCollection)` (where `aCollection` is anything that uses the Node kernel calls and exposes `elements`) will expand into loops where `val` is each value in the set, in order. `val` needs not be defined beforehand. With `PHIL_LDMSTM` enabled you can also use `&val` as a reference to a `val` you *did* define beforehand. Semantics on the "foreach an object" side are *way* different from Phil's!
* `KAWA_NOTRANSPARANCYNAG` - Disables the confirmation dialog when pasting an image that contains the transparant color. Annoying!
* `KAWA_HEXFONTS` - Makes the font editor's character picker labels display in hexadecimal, to match Windows Character Map.
* `KAWA_FONTLIMITBREAK` - Disables the 256-cel limit on fonts (but also maybe views, so be careful!). Fonts have a 16-bit character count, and with a customized interpreter you can now have basic UTF-8!
* `KAWA_VOCABPREVIEWS` - Adds some more sidebar previews for vocabs, like the Object Offsets (994), ~~and the opcode list (998)~~. Also changes the format for kernel names (999).
* `KAWA_DISPLAYMASSAGE` - Makes the `Display` kernel call's arguments look better, with proper constants instead of numbers, so `dsWIDTH` instead of `106`.

There are a few other changes that aren't defined away, such as the *Shrinkwrap cel* menu item. In the `Debug` and `Release` target, none of the above are enabled. In `Mild`, only `KAWA_VOCABPREVIEWS` and `KAWA_DISPLAYMASSAGE` are, unless someone wishes otherwise. The `Kawa` target has all of them enabled *but* `PHIL_LDMSTM`.
