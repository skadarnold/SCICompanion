.. &exists

.. include:: /includes/standard.rst

============
 &exists
============

This command makes variadic functions more readable by letting you name optional arguments instead of counting them.

Example::

	(if (&exists theObj)		(= client theObj)
		(if (&exists dist)	(= distance dist)
		)
	)
	
	; which is somewhat easier to understand but ultimately compiles the same as this:
	
	(if (>= argc 1)			(= client theObj)
		(if (>= argc 2)		(= distance dist)
		)
	)
