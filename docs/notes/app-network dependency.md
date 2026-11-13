### Important!

The :app module now DOES NOT see :core:network module.  
It must be taken into account that Hilt may not see :core:networks' classes in compile time.
The dependency is as follows:

:app

--->

:core:data (implementation of :core:network)

--->

:core:network

