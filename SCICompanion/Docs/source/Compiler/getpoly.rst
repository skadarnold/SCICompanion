.. &getpoly

.. include:: /includes/standard.rst

============
 &getpoly
============

This command added by Kawa lets you easily produce bytecode that looks like what you would find in Sierra SCI games, without losing the flexibility of the Picture Editor's polygon editing mode.

Example::

	(method (init) 
		(gRoom addObstacle: (&getpoly "") (&getpoly "pillar"))
		(super init:)
	)	
