;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 This script contains :class:`Conversation`, which lets you assemble multiple messages together
;	 in a sequence, even if they have separate noun/verb/cond/seq tuples.
(script# CONVERSATION_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use Print)
(use System)


;	
;	 This class works in
;	 conjunction with :class:`Conversation`.
(class MessageObj of Object
	(properties
		modNum -1
		noun 0
		verb 0
		case 0
		sequence 0
		whoSays 0
		client 0
		caller 0
		font 0
		x 0
		y 0
	)
	
	(method (showSelf &tmp [temp0 40])
		(= whoSays
			(gMessager
				findTalker: (Message
					msgGET
					modNum
					noun
					verb
					case
					(if sequence else 1)
				)
			)
		)
		(if (not (IsObject whoSays))
			(Print
				addTextF:
					{<MessageObj> Message not found: %d - %d, %d, %d, %d}
					modNum
					noun
					verb
					case
					sequence
				init:
			)
			(= gQuitGame TRUE)
		else
			(if font (whoSays font: font))
			(if (or x y) (whoSays x: x y: y))
			(gMessager say: noun verb case sequence caller modNum)
		)
	)
)

;	
;	 This seems to be a class to which you can add a series of messages that will be displayed one
;	 after another. :class:`Messager` does this automatically, but only if messages have the same noun/verb/cond tuple
;	 and sequentially increasing sequence numbers.
(class Conversation of List
	(properties
		elements 0
		size 0
		script 0
		curItem -1
		caller 0
	)
	
	(method (init theCaller)
		(= curItem -1)
		(if (and argc (IsObject theCaller))
			(= caller theCaller)
		)
		(gTheDoits add: self)
		(self cue:)
	)
	
	(method (doit)
		(if script (script doit:))
	)
	
	(method (dispose &tmp theCaller)
		(self eachElementDo: #perform cleanCode)
		(gTheDoits delete: self)
		(if gDialog (gDialog dispose:))
		(if script (= script 0))
		(= theCaller caller)
		(super dispose:)
		(if theCaller (theCaller cue:))
	)
	
	;
	; .. function:: add([moduleNumber noun verb condition sequence x y font)
	;
	; 	Adds a new message to the conversation.
	;
	; 	:param number moduleNumber: Room number, or -1 for the current room.
	; 	:param number noun: The message noun.
	; 	:param number verb: The message verb.
	; 	:param number condition: The message condition.
	; 	:param number sequence: The message sequence.
	; 	:param number x: The message x position.
	; 	:param number y: The message y position.
	; 	:param number font: The message font.
	;
	(method (add param &tmp theGModNum theNoun theVerb theCase theSeq theX theY theFont)
		(= theSeq 0)
		(= theCase theSeq)
		(= theVerb theCase)
		(= theNoun theVerb)
		(= theGModNum theNoun)
		(= theFont 0)
		(= theY theFont)
		(= theX theY)
		(if (and argc (not (IsObject [param 0])))
			(= theGModNum [param 0])
			(if (== theGModNum -1) (= theGModNum gRoomNumber))
			(if (> argc 1)
				(= theNoun [param 1])
				(if (> argc 2)
					(= theVerb [param 2])
					(if (> argc 3)
						(= theCase [param 3])
						(if (> argc 4)
							(= theSeq [param 4])
							(if (> argc 5)
								(= theX [param 5])
								(if (> argc 6)
									(= theY [param 6])
									(if (> argc 7) (= theFont [param 7]))
								)
							)
						)
					)
				)
			)
			(if (not (IsObject [param 0]))
				(super
					add:
						((MessageObj new:)
							modNum: theGModNum
							noun: theNoun
							verb: theVerb
							case: theCase
							sequence: theSeq
							x: theX
							y: theY
							font: theFont
							yourself:
						)
				)
			)
		else
			(super add: param &rest)
		)
	)
	
	(method (cue param1 &tmp temp0 temp1)
		(if (or (and argc param1) (== (++ curItem) size))
			(self dispose:)
		else
			(= temp0 (self at: curItem))
			(cond 
				((temp0 isKindOf: MessageObj) (temp0 caller: self showSelf:))
				((temp0 isKindOf: Script) (self setScript: temp0 self))
				((IsObject temp0) (temp0 doit: self))
				(else (self cue:))
			)
		)
	)
	
	(method (setScript theScript)
		(if (IsObject script) (script dispose:))
		(if theScript (theScript init: self &rest))
	)
	
	(method (load param1 &tmp theGModNum temp1 temp2 temp3 temp4 temp5 temp6 temp7 temp8)
		(= theGModNum (WordAt param1 0))
		(= temp1 (WordAt param1 1))
		(= temp2 (WordAt param1 2))
		(= temp3 (WordAt param1 3))
		(= temp4 (WordAt param1 4))
		(= temp5 (WordAt param1 5))
		(= temp6 (WordAt param1 6))
		(= temp7 (WordAt param1 7))
		(= temp8 7)
		(while theGModNum
			(if (== theGModNum -1) (= theGModNum gRoomNumber))
			(self
				add: theGModNum temp1 temp2 temp3 temp4 temp5 temp6 temp7
			)
			(= theGModNum (WordAt param1 (++ temp8)))
			(= temp1 (WordAt param1 (++ temp8)))
			(= temp2 (WordAt param1 (++ temp8)))
			(= temp3 (WordAt param1 (++ temp8)))
			(= temp4 (WordAt param1 (++ temp8)))
			(= temp5 (WordAt param1 (++ temp8)))
			(= temp6 (WordAt param1 (++ temp8)))
			(= temp7 (WordAt param1 (++ temp8)))
		)
	)
)

(instance cleanCode of Code
	(properties)
	
	(method (doit param1 &tmp temp0)
		(if (param1 isKindOf: Script) (param1 caller: 0))
		(if
			(and
				(param1 isKindOf: MessageObj)
				(IsObject (= temp0 (param1 whoSays?)))
				(temp0 underBits?)
			)
			(temp0 dispose: 1)
		)
	)
)
