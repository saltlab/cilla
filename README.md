Cilla
=====

CSS is a widely used language for describing the
presentation semantics of HTML elements on the web. The
language has a number of characteristics, such as inheritance
and cascading order, which makes maintaining CSS code a
challenging task for web developers. As a result, it is common
for unused rules to be accumulated over time. Despite these
challenges, CSS analysis has not received much attention from
the research community. We propose an automated technique
to support styling code maintenance, which (1) analyzes the
runtime relationship between the CSS rules and DOM elements
of a given web application (2) detects unmatched and ineffective
selectors, overridden declaration properties, and undeﬁned
class values. Our technique, implemented in an open source
tool called CILLA, has a high precision and recall rate. The
results of our case study, conducted on ﬁfteen open source and
industrial web-based systems, show an average of 60% unused
CSS selectors in deployed applications, which points to the
ubiquity of the problem.

### Paper

[Automated Analysis of CSS Rules to Support Style Maintenance, ICSE'12](http://www.ece.ubc.ca/~amesbah/docs/icse12.pdf)

