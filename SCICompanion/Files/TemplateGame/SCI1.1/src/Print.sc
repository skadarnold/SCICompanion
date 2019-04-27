;;; Sierra Script 1.0 - (do not remove this comment)
; This script contains a number of useful procedures for printing text on the screen.
(script# 921)
(include sci.sh)
(use Main)
(use DColorButton)
(use Controls)
(use DialogControls)
(use System)

(public
	Prints 0
	Printf 1
	GetInput 2
	FindFormatLen 3
)

;	
;	 Prints text on the screen.
;	
;	 :param string theText: A string of text to print.
(procedure (Prints)
	(Print addText: &rest init:)
)

;
; .. function:: Printf(theText [params ...])
;
; 	Prints text on the screen using. The text may contain the following formatting
; 	characters:
; 	
; 	%d
; 		Formats a number in decimal.
; 		
; 	%x
; 		Formats a number in hexadecimal.
;
; 	%s
; 		Formats a string.
;
; 	:param string theText: A string of text containing formatting characters.
; 	
; 	Example usage::
; 	
; 		(Printf "Your name is %s, and you have %d hit points" name hitPoints)
; 	
(procedure (Printf)
	(Print addTextF: &rest init:)
)

;
; .. function:: GetInput(buffer maxLength [title font])
;
; 	Displays an edit box so the player can enter text.
; 	
; 	:param string buffer: A buffer in which to place the text that was entered.
; 	:param number maxLength: The size of the text buffer.
; 	:param string title: An optional title for the dialog.
; 	:param number font: An optional font number.
; 	:returns: The length of the text that was input.
; 	
; 	Example usage::
; 		
; 		(method (SomeMethod &tmp [buffer 10])
; 			(= buffer NULL)	// Set the first character to NULL to create an empty string
; 			(GetInput buffer 10 "Enter your name")
; 		)
; 	
; .. IMPORTANT::
; 	The buffer is also used to initialize the edit field, so it should contain an empty string if you don't want any text there to start with.
(procedure (GetInput buffer maxLength title theFont)
	(if
		(Print
			font: (if (> argc 3) theFont else gFont)
			addText: (if (and (> argc 2) title) title else {})
			addEdit: buffer maxLength 0 12 buffer
			init:
		)
		(StrLen buffer)
	)
)

;
; .. function:: FindFormatLen(theText [params ...])
;
; 	Gets the maximum possible length of a formatted string.
; 	
; 	:param string theText: Text with format specifiers (%d, %x, %s).
; 	:param params: Optional parameters that correspond to format specifiers.
; 	:returns: The maximum length the resulting formatted string could be.
(procedure (FindFormatLen theText params &tmp temp0 temp1 temp2 temp3)
	(= temp1 (StrLen theText))
	(= temp0 temp1)
	(= temp2 0)
	(= temp3 0)
	(while (< temp3 temp1)
		(if (== (StrAt theText temp3) 37)   ; %
			(switch (StrAt theText (++ temp3))
				(KEY_d (= temp0 (+ temp0 5)))
				(KEY_x (= temp0 (+ temp0 4)))
				(KEY_s
					(= temp0 (+ temp0 (StrLen [params temp2])))
				)
			)
			(++ temp2)
		)
		(++ temp3)
	)
	(return (++ temp0))
)

;	
;	 The Print class lets you show text, buttons and images in a GUI on screen.
;	
;	 It is generally used as a static class, in the sense that you call methods on Print
;	 directly without making an instance of it. The init: method then displays the dialog.
;	
;	 Example usage::
;	
;	 	(Print
;	 		addTitle: "This is the title"
;	 		font: 0
;	 		width: 90
;	 		addText: "Enter new font number:"
;	 		addEdit: @temp0 6 0 24
;	 		addButton: 0 "A button!" 0 12
;	 		init:
;	 	)
;	
(class Print of Object
	(properties
		dialog 0
		window 0
		title 0             ; The dialog title.
		mode alLEFT         ; Default alignment: alLEFT, alCENTER or alRIGHT.
		font -1
		width 0
		x -1
		y -1
		ticks 0
		caller 0
		retValue 0
		modeless FALSE
		first 0
		saveCursor 0
	)
	
	;
	; .. function:: init([theCaller text])
	;
	; 	Calls showSelf() to show the dialog.
	;
	; 	:param heapPtr theCaller: An optional object with a cue method.
	; 	:param string text: Optional text that is added to the dialog.
	; 	:returns: If a button was pressed, returns the value of that button.
	;
	(method (init theCaller)
		(= caller 0)
		(if argc (= caller theCaller))
		(if (> argc 1) (self addText: &rest))
		(if (not modeless)
			(if (not (IsObject gPrints))
				(= gPrints ((EventHandler new:) name: {prints}))
			)
			(gPrints add: self)
		)
		(self showSelf:)
	)
	
	(method (doit)
		(dialog eachElementDo: #doit)
	)
	
	(method (dispose)
		(if (and gPrints (gPrints contains: self))
			(gPrints delete: self)
			(if (gPrints isEmpty:)
				(gPrints dispose:)
				(= gPrints NULL)
			)
		)
		(if title (Memory memFREE title))
		(= width
			(= mode
				(= title (= first (= saveCursor (= window 0))))
			)
		)
		(= x (= y -1))
		(= modeless 0)
		(gSounds pause: FALSE)
		(super dispose:)
	)
	
	; Shows the dialog. Generally, you should use the init() method to show the dialog.
	(method (showSelf &tmp theFirst temp1 temp2 temp3 temp4)
		(if saveCursor (gGame setCursor: 999))
		(if (not dialog) (= dialog (Dialog new:)))
		(dialog
			window: (if window else gWindow)
			name: {PODialog}
			caller: self
		)
		(dialog text: title time: ticks setSize:)
		(dialog center:)
		(= temp3 (if (== x -1) (dialog nsLeft?) else x))
		(= temp4 (if (== y -1) (dialog nsTop?) else y))
		(dialog moveTo: temp3 temp4)
		(= temp1 (GetPort))
		(dialog open: (if title nwTITLE else nwNORMAL) 15)
		(return
			(if modeless
				(= gOldPort (GetPort))
				(SetPort temp1)
				(= gDialog dialog)
			else
				(gSounds pause: TRUE)
				(= theFirst first)
				(cond 
					((not theFirst)
						(= theFirst (dialog firstTrue: #checkState csENABLED))
						(if
							(and
								theFirst
								(not (dialog firstTrue: #checkState csEXIT))
							)
							(theFirst state: (| (theFirst state?) csEXIT))
						)
					)
					((not (IsObject theFirst)) (= theFirst (dialog at: theFirst)))
				)
				(= retValue (dialog doit: theFirst))
				(SetPort temp1)
				(cond 
					((== retValue -1) (= retValue 0))
					(
					(and (IsObject retValue) (retValue isKindOf: DButton)) (= retValue (retValue value?)))
					((not (dialog theItem?)) (= retValue 1))
				)
				(if saveCursor
					(gGame setCursor: ((gIconBar curIcon?) cursor?))
				)
				(dialog dispose:)
				(return retValue)
			)
		)
	)
	
	;
	; .. function:: addButton(theValue noun verb cond seq [x y modNum])
	;
	; .. function:: addButton(theValue text [x y])
	;
	; 	Adds a button to the dialog, either with the supplied text or a message resource. 
	;
	; 	:param number theValue: A numerical value associated with the button. This value will be returned by the dialog if the button is pressed.
	; 	:param string text: The button text.
	; 	:param number x: The button x offset from the upper left of the dialog.
	; 	:param number y: The button y offset from the upper left of the dialog.
	; 	:param number noun: The noun of the message to show.
	; 	:param number verb: The verb of the message to show.
	; 	:param number cond: The condition of the message to show.
	; 	:param number seq: The sequence of the message to show, or 0.
	; 	:param number modNum: Optional room number of the message to show (if not specified, the current room is used).
	;
	(method (addButton theValue params &tmp noun verb case seq x y modNum text temp8)
		(if (not dialog) (= dialog (Dialog new:)))
		(if (== font -1) (= font gFont))
		(if (> argc 4)
			(= noun [params 0])
			(= verb [params 1])
			(= case [params 2])
			(= seq (if [params 3] [params 3] else 1))
			(= x 0)
			(= y 0)
			(= modNum gRoomNumber)
			(if (> argc 5)
				(= x [params 4])
				(if (> argc 6)
					(= y [params 5])
					(if (> argc 7) (= modNum [params 6]))
				)
			)
			(= temp8 (Message msgSIZE modNum noun verb case seq))
			(if temp8
				(= text
					(Memory
						memALLOC_CRIT
						(= temp8 (Message msgSIZE modNum noun verb case seq))
					)
				)
				(if
				(not (Message msgGET modNum noun verb case seq text))
					(= text 0)
				)
			)
		else
			(= x 0)
			(= y 0)
			(if (> argc 2)
				(= x [params 1])
				(if (> argc 3) (= y [params 2]))
			)
			(= text
				(Memory memALLOC_CRIT (+ (StrLen [params 0]) 1))
			)
			(StrCpy text [params 0])
		)
		(if text
			(dialog
				add:
					((DButton new:)
						value: theValue
						font: font
						text: text
						setSize:
						moveTo: (+ 4 x) (+ 4 y)
						yourself:
					)
				setSize:
			)
		)
	)
	
	;
	; .. function:: addColorButton(theValue noun verb cond seq [x y modNum normalForeColor highlightForeColor selectedForeColor normalBackColor highlightBackColor selectedBackColor])
	; .. function:: addColorButton(theValue text [x y normalForeColor highlightForeColor selectedForeColor normalBackColor highlightBackColor selectedBackColor])
	;
	; 	This is similar to addButton, but lets you specify up to 6 color indices for various parts and states of the button.
	;
	; 	:param number theValue: A numerical value associated with the button. This value will be returned by the dialog if the button is pressed.
	; 	:param string text: The button text.
	; 	:param number x: The button x offset from the upper left of the dialog.
	; 	:param number y: The button y offset from the upper left of the dialog.
	; 	:param number noun: The noun of the message to show.
	; 	:param number verb: The verb of the message to show.
	; 	:param number cond: The condition of the message to show.
	; 	:param number seq: The sequence of the message to show, or 0.
	; 	:param number modNum: Optional room number of the message to show (if not specified, the current room is used).
	;
	(method (addColorButton theValue params &tmp noun verb case seq x y modNum temp7 temp8 normalForeColor normalBackColor highlightForeColor highlightBackColor selectedForeColor selectedBackColor)
		(if (not dialog) (= dialog (Dialog new:)))
		(if (== font -1) (= font gFont))
		(= normalForeColor 0)
		(= highlightForeColor 15)
		(= selectedForeColor 31)
		(= normalBackColor 5)
		(= highlightBackColor 5)
		(= selectedBackColor 5)
		(if (< (Abs [params 0]) 1000)
			(= noun [params 0])
			(= verb [params 1])
			(= case [params 2])
			(= seq (if [params 3] [params 3] else 1))
			(= x 0)
			(= y 0)
			(= modNum gRoomNumber)
			(if (> argc 5)
				(= x [params 4])
				(if (> argc 6)
					(= y [params 5])
					(if (> argc 7)
						(= modNum [params 6])
						(if (> argc 8)
							(= normalForeColor [params 7])
							(if (> argc 9)
								(= highlightForeColor [params 8])
								(if (> argc 10)
									(= selectedForeColor [params 9])
									(if (> argc 11)
										(= normalBackColor [params 10])
										(if (> argc 12)
											(= highlightBackColor [params 11])
											(if (> argc 13) (= selectedBackColor [params 12]))
										)
									)
								)
							)
						)
					)
				)
			)
			(= temp8 (Message msgSIZE modNum noun verb case seq))
			(if temp8
				(= temp7
					(Memory
						memALLOC_CRIT
						(= temp8 (Message msgSIZE modNum noun verb case seq))
					)
				)
				(if
				(not (Message msgGET modNum noun verb case seq temp7))
					(= temp7 0)
				)
			)
		else
			(= x 0)
			(= y 0)
			(if (> argc 2)
				(= x [params 1])
				(if (> argc 3)
					(= y [params 2])
					(if (> argc 4)
						(= normalForeColor [params 3])
						(if (> argc 5)
							(= highlightForeColor [params 4])
							(if (> argc 6)
								(= selectedForeColor [params 5])
								(if (> argc 7)
									(= normalBackColor [params 6])
									(if (> argc 8)
										(= highlightBackColor [params 7])
										(if (> argc 9) (= selectedBackColor [params 8]))
									)
								)
							)
						)
					)
				)
			)
			(= temp7
				(Memory memALLOC_CRIT (+ (StrLen [params 0]) 1))
			)
			(StrCpy temp7 [params 0])
		)
		(if temp7
			(dialog
				add:
					((DColorButton new:)
						value: theValue
						font: font
						text: temp7
						mode: mode
						nfc: normalForeColor
						nbc: normalBackColor
						sfc: selectedForeColor
						sbc: selectedBackColor
						hfc: highlightForeColor
						hbc: highlightBackColor
						setSize: width
						moveTo: (+ 4 x) (+ 4 y)
						yourself:
					)
				setSize:
			)
		)
	)
	
	;
	; .. function:: addEdit(buffer maxLength [x y initialText])
	;
	; 	Adds an edit control to the dialog.
	;
	; 	:param string buffer: The buffer that will receive the text.
	; 	:param number maxLength: The length of the buffer.
	; 	:param number x: The edit control x offset from the upper left of the dialog.
	; 	:param number y: The edit control y offset from the upper left of the dialog.
	; 	:param string initialText: Text to initialize the edit control.
	;
	(method (addEdit theBuffer maxLength x y initialText &tmp theX theY)
		(if (not dialog) (= dialog (Dialog new:)))
		(StrCpy theBuffer (if (> argc 4) initialText else {}))
		(if (> argc 2) (= theX x) (if (> argc 3) (= theY y)))
		(dialog
			add:
				((DEdit new:)
					text: theBuffer
					max: maxLength
					setSize:
					moveTo: (+ theX 4) (+ theY 4)
					yourself:
				)
			setSize:
		)
	)
	
	;
	; .. function:: addIcon(view loop cel [x y])
	;
	; 	Adds an icon to the dialog.
	;
	(method (addIcon theView theLoop theCel theX theY &tmp temp0 temp1)
		(if (not dialog) (= dialog (Dialog new:)))
		(if (> argc 3)
			(= temp0 theX)
			(= temp1 theY)
		else
			(= temp1 0)
			(= temp0 temp1)
		)
		(if (IsObject theView)
			(dialog
				add: (theView
					setSize:
					moveTo: (+ temp0 4) (+ temp1 4)
					yourself:
				)
				setSize:
			)
		else
			(dialog
				add:
					((DIcon new:)
						view: theView
						loop: theLoop
						cel: theCel
						setSize:
						moveTo: (+ temp0 4) (+ temp1 4)
						yourself:
					)
				setSize:
			)
		)
	)
	
	;
	; .. function:: addText(noun verb cond seq [x y modNum])
	;
	; .. function:: addText(text [x y])
	;
	; 	Adds text to the dialog, either with the supplied string or a message resource. 
	;
	; 	:param string text: The text.
	; 	:param number x: The text x offset from the upper left of the dialog.
	; 	:param number y: The text y offset from the upper left of the dialog.
	; 	:param number noun: The noun of the message to show.
	; 	:param number verb: The verb of the message to show.
	; 	:param number cond: The condition of the message to show.
	; 	:param number seq: The sequence of the message to show, or 0.
	; 	:param number modNum: Optional room number of the message to show (if not specified, the current room is used).
	;
	(method (addText params &tmp noun verb case seq x y modNum text temp8)
		(if (not dialog) (= dialog (Dialog new:)))
		(if (== font -1) (= font gFont))
		(if (> argc 3)
			(= noun [params 0])
			(= verb [params 1])
			(= case [params 2])
			(= seq (if [params 3] [params 3] else 1))
			(= x 0)
			(= y 0)
			(= modNum gRoomNumber)
			(if (>= argc 5)
				(= x [params 4])
				(if (>= argc 6)
					(= y [params 5])
					(if (>= argc 7) (= modNum [params 6]))
				)
			)
			(= temp8 (Message msgSIZE modNum noun verb case seq))
			(if temp8
				(= text
					(Memory
						memALLOC_CRIT
						(= temp8 (Message msgSIZE modNum noun verb case seq))
					)
				)
				(if (Message msgGET modNum noun verb case seq text)
					(dialog
						add:
							((DText new:)
								text: text
								font: font
								mode: mode
								setSize: width
								moveTo: (+ 4 x) (+ 4 y)
								yourself:
							)
						setSize:
					)
				)
			)
		else
			(= x 0)
			(= y 0)
			(if (>= argc 2)
				(= x [params 1])
				(if (>= argc 3) (= y [params 2]))
			)
			(= text
				(Memory memALLOC_CRIT (+ (StrLen [params 0]) 1))
			)
			(StrCpy text [params 0])
			(dialog
				add:
					((DText new:)
						text: text
						font: font
						mode: mode
						setSize: width
						moveTo: (+ 4 x) (+ 4 y)
						yourself:
					)
				setSize:
			)
		)
	)
	
	; Adds formatted text to the dialog. There are no options for positioning or getting the text from a message resource.
	(method (addTextF &tmp temp0 temp1)
		(= temp0 (FindFormatLen &rest))
		(= temp1 (Memory memALLOC_CRIT temp0))
		(Format temp1 &rest)
		(self addText: temp1)
		(Memory memFREE temp1)
	)
	
	;
	; .. function:: addTitle(noun verb cond seq [modNum])
	;
	; .. function:: addTitle(text)
	;
	; 	Adds a title to the dialog, either with the supplied string or a message resource. 
	;
	; 	:param string text: The title text.
	; 	:param number noun: The noun of the message to show.
	; 	:param number verb: The verb of the message to show.
	; 	:param number cond: The condition of the message to show.
	; 	:param number seq: The sequence of the message to show, or 0.
	; 	:param number modNum: Optional room number of the message to show (if not specified, the current room is used).
	;
	(method (addTitle params &tmp noun verb case seq modNum temp5)
		(if (> argc 1)
			(= noun [params 0])
			(= verb [params 1])
			(= case [params 2])
			(= seq [params 3])
			(= modNum [params 4])
			(= temp5 (Message msgSIZE modNum noun verb case seq))
			(if temp5
				(= title
					(Memory
						memALLOC_CRIT
						(= temp5 (Message msgSIZE modNum noun verb case seq))
					)
				)
				(Message msgGET modNum noun verb case seq title)
			)
		else
			(= title
				(Memory memALLOC_CRIT (+ (StrLen [params 0]) 1))
			)
			(StrCpy title [params 0])
		)
	)
	
	; Positions the dialog on screen.
	(method (posn theX theY)
		(= x theX)
		(= y theY)
	)
	
	(method (handleEvent pEvent)
		(if (dialog handleEvent: pEvent) (dialog dispose:))
	)
	
	(method (cue &tmp theCaller)
		(= theCaller caller)
		(= dialog 0)
		(if window (window dispose:))
		(self dispose:)
		(if theCaller (theCaller cue:))
	)
)
