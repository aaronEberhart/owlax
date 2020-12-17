# Evaluation of OWLAx Pattern Expressibility

This code was used to perfom the evaluation that producd the data in [this spreadsheet](https://tinyurl.com/eswc2021).

### [Javadoc](https://aaroneberhart.github.io/owlax/)

## Notes

This links to the copy of [GO](https://bioportal.bioontology.org/ontologies/GO-PLUS) used in the evaluation. If you dowload it and place it in the OWL directory it should run correctly. 

The LOV files were automatically pulled from the [LOV dump](https://lov.linkeddata.es/lov.nq.gz) using the python script. Most work but some will cause OWL API to hang for a while. If you run the evaluaton with the function analyzing LOV commented out in Main it will run much faster.
