# Solution:


First thing that comes to mind after reading the challenge: double charging of invoices must be prevented. In this open ended challenge I decided to focus my attention to that aspect of the solution due to time constraints, even though there are many other design decisions to be considered.



## no double charges:

In a single-threaded system, this is not an issue, as each invoice will be processed sequentially. It is worth it to consider however, what happens when we use multiple threads, or even multiple application instances connected to a single database. An invoice that is being processed, but not yet finished, could be picked up by another thread/instance, potentially resulting in a double charging of the invoice.

I see 3 ways to deal with the aforementioned issue:


- db transactions/locking:
  In a distributed system, we can use db transactions such that only a single instance can update a specific record at a given time. When processing of an invoice starts, a “PROCESSING”-state can atomically be set to lock the invoice. After processing, the invoice state is set to either “PAID” or back to “PENDING”. In a single, multi-threaded system we can use locks, which will be demonstrated with some code.

Drawback: not all db’s offer transaction support

- central job queue:
  a straightforward approach is to use a single central job queue that multiple workers can pick jobs from, ensuring that each job is only executed once.

Drawback: single point of failure

- distributed mutual exclusion:
  An algorithm to allow multiple worker nodes in a distributed system to share access to a resource without interfering with each other.

Drawback: difficult to implement/test

# Implementation

I decided to implement a locking mechanism for the invoices. When an invoice is being processed, a lock is acquired by the running thread, which prevents other threads from processing that same invoice until the lock is released.
Some additional utility methods were written, and a test with multiple threads is provided.