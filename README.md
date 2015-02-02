# pengyifan-anaphoraresolution
An implementation of the classic Resolution of Anaphora Procedure (RAP)

###JavaRAP

JavaRAP is an implementation of the classic Resolution of Anaphora Procedure (RAP) given by [Lappin and Leass (1994)](http://acl.ldc.upenn.edu/J/J94/J94-4002.pdf) (Accuracy: 57.9%). It resolves third person pronouns, lexical anaphors, and identifies pleonastic pronouns. The original purpose of the implementation is to provide anaphora resolution result to our TREC 2003 Q&A system. Since RAP is so widely known, we shortly came to the idea of making this implementation freely available to the research community, in the hope that it could 
* be used as a reference to benchmark other anaphora resolution algorithms or systems; and
* provide anaphora resolution function as needed by other NLP applications. 

We name the implementation as JavaRAP because it is developed in Java. Such a decision in programming language makes it more easily portable over hardware platforms, however less straightforwardly when the operating system changes. 

### Developers

* Yifan Peng (yfpeng@udel.edu)

### Acknowledgment

This project is based on [JavaRAP](http://aye.comp.nus.edu.sg/~qiu/NLPTools/JavaRAP.html), which was developed by Long Qiu. The latest version 1.13 was released on Jan 19, 2011 and has not been updated since then.

###Reference

* Long Qiu, Min-Yen Kan and Tat-Seng Chua. (2004).  A Public Reference Implementation of the RAP Anaphora Resolution Algorithm.  In proceedings of  the Fourth International Conference on Language Resources and Evaluation (LREC 2004). Vol. I, pp. 291-294.
