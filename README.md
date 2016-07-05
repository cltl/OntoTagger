OntoTagger
==========
version 3.1.1
Copyright: VU University Amsterdam, Piek Vossen
email: piek.vossen@vu.nl
website: www.newsreader-project.eu
website: cltl.nl

SOURCE CODE:

https://github.com/cltl/OntoTagger

INSTALLATION:
1. git clone https://github.com/cltl/OntoTagger.git
2. cd OntoTagger
3. chmod +wrx install.sh
4. ./install.sh

Installation through apache-maven-2.2.1 on the basis of the pom.xml

REQUIREMENTS
OntoTagger is developed in Java 1.6 and can run on any platform that supports Java 1.6

LICENSE
    OntoTagger is free software: you can redistribute it and/or modify
    it under the terms of the The Apache License, Version 2.0:
        http://www.apache.org/licenses/LICENSE-2.0.txt.

    OntoTagger is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

DESCRIPTION

Ontotagger is package with various functions to insert semantic tags tyo elements in NAF.
The input is a NAF stream and the result is a NAF output stream with enrinched layers.
The main functions are:

1. Insert PredicateMatrix into the term layer with WSD output (script predicate-matrix-tagger.sh)

The predicate-matrix-tagger.sh script reads a NAF file and adds the PredicateMatrix mappings to each sense in the term layer.
The mappings are read from the PredicateMatrix file that should be installed as part of the vua-resources package.
Example call:

cat ../examples/naf.xml | ./predicate-matrix-tagger.sh

Requires a term layer with wordnet synsets.

2. Adding FrameNet and other PredicateMatrix mappings to the SRL layer on the basis of mapping relations (script: srl-framenet-tagger.sh)
The srl-framenet-tagger.sh script reads a NAF input stream with a SRL layer and PredicateMatrix mappings added to the term layer.
It will add FrameNet frames from the term layer to the predicate in the SRL layer and also try to resolve the role mappings for FrameNet elements. 
In addition to Framenet frames and elements, also ESO types and roles are mapped.
Example call:

cat ../examples/naf.xml | ./srl-framenet-tagger.sh.sh

Requires a term layer with PredicateMatrix data added to the term layer and a SRL layer.

3. Detecting nominal events (script: nominal-event.sh)

The nominal-event.sh script checks the term layer for nouns that can have event meaning according to the nl-luIndex.xml file.
The script requires the nl-luIndex.xml to be present and to be installed as part of the vua-resources package.
In the nl-luIndex.xml file, we provided mappings from FrameNet frames to Dutch nouns and verbs. If a nominal term is in this file,
the program adds the FrameNet frames and adds the term as a predicate to the SRL layer.

Example call:

cat ../examples/naf.xml | ./nominal-events.sh

Requires the presence of a SRL layer and a term layer.

These functions have been used mainly within the Dutch NewsReader pipeline.

=====================================
Some older functions that may still work: 

1. tag terms with wordnet synsets with base concepts and/or ontology mappings
2. the same as 1. for a whole folder of KAF/NAF files
3. tag verb terms having synsets with SemLink mappings as they are stored in a Predicate Matrix file
4. the same as 3. but for a whole folder with KAF/NAF files.
5. tag lemmas of terms with an RDF ontology with labels. Matches the term lemmas with the labels in RDF and inserts the RDF classification accordingly

1. Tag a single KAF file with base concepts and ontology labels

Class: eu.kyotoproject.main.KafOntotagger

Description: this function reads the term layer, looks for a synset in the externalReferences and
checks the synset identifier in the provided data files. If it finds the synset in the base concept list
it will insert it as an externalReference to the synset element. If it finds the synset id in the synset-to-ontology
list then it inserts it as an externalReference to the synset element as well. In case an ontology-to-ontology
file is provided, the inserted ontology label is searched in the ontology-to-ontology mapping and all mappings
are inserted as a child of the inserted ontology label. This represents the explicit ontology classification
from the ontology for the synset.

A relation file can be provided to select particular mapping relations only. If the relation file is omitted,
all mappings are inserted.

Usage:

parameters:
--kaf-file              <path to the kaf file>
--synset-baseconcept    [optional] <path to a text file with synset to base concept mappings>
--synset-ontology       [optional] <path to a text file with synset to ontology label mappings>
--ontology-ontology     [optional] <path to the explicit ontology relations, assumes --synset-ontology>
--relations             [optional] <path to a text file with the relations accepted>
--format                <values "naf" or "kaf": indicate the format of the output>

Format of the --synset-baseconcept file:
syntax: synset-id+space+synset-id

dw-eng-30-100-n eng-30-04341686-n
dw-eng-30-101-n eng-30-03391770-n
dw-eng-30-102-n eng-30-04341686-n
dw-eng-30-103-n eng-30-04341686-n


Format of the --synset-ontology file:
syntax: synset-id+space+relation+ontology-label

dw-eng-30-100-n sc_hasPart Kyoto#land__ground__soil-eng-3.0-09335240-n
dw-eng-30-100-n sc_hasState Kyoto#simple
dw-eng-30-100-n sc_subClassOf Kyoto#structure__construction-eng-3.0-04341686-n
dw-eng-30-101-n sc_subClassOf Kyoto#framework-eng-3.0-03391770-n
dw-eng-30-102-n sc_hasCoParticipant DomainKyoto2#vehicle-eng-3.0-04524313-n
dw-eng-30-102-n sc_playCoRole FunctionalParticipation.owl#product
dw-eng-30-102-n sc_subClassOf Kyoto#structure__construction-eng-3.0-04341686-n
dw-eng-30-103-n sc_subClassOf Kyoto#structure__construction-eng-3.0-04341686-n
dw-eng-30-104-n sc_subClassOf Kyoto#structure__construction-eng-3.0-04341686-n
dw-eng-30-105-n sc_hasState Kyoto#vital

Format of the --ontology-ontology file:
synstax: ontology-label+space+relation+ontology-label

DomainKyoto2#ability SubClassOf DomainKyoto2#ability inherited
DomainKyoto2#ability SubClassOf TopKyoto3#dispositional
DomainKyoto2#ability DOLCE-Lite.owl#q-location TopKyoto2.1#dispositional-region inherited
DomainKyoto2#ability SubClassOf DOLCE-Lite.owl#quality inherited
DomainKyoto2#ability DOLCE-Lite.owl#inherent-in DOLCE-Lite.owl#endurant inherited
DomainKyoto2#ability SubClassOf DOLCE-Lite.owl#spatio-temporal-particular inherited
DomainKyoto2#ability SubClassOf DOLCE-Lite.owl#particular inherited

Output:

Output is the KAF files as a text output stream.

The next example shows 3 meanings in WordNet for the term people. The first meaning only got a base-concept mapping.
The other two meanings got a base-concept mapping and have a mapping to the ontology that is expanded to the full type
hierarchy according to the KYOTO extension of DOLCE.

<term lemma="people" pos="NNS" tid="t2" type="open">
<span>
<target id="w2"/>
</span>
<externalReferences>
<externalRef confidence="0.918609" refType="" reference="eng-30-07942152-n" resource="wn30g_eng.v20" status="">
    <externalRef confidence="0.0" refType="base-concept" reference="eng-30-00031264-n" resource="" status=""/>
</externalRef>
<externalRef confidence="0.0567295" refType="" reference="eng-30-08160276-n" resource="wn30g_eng.v20" status="">
    <externalRef confidence="0.0" refType="base-concept" reference="eng-30-00031264-n" resource="" status=""/>
    <externalRef confidence="0.0" refType="sc_subClassOf" reference="Kyoto#group__grouping-eng-3.0-00031264-n" resource="" status="">
            <externalRef confidence="0.0" refType="SubClassOf" reference="ExtendedDnS.owl#non-agentive-social-object" resource="" status="implied">
                <externalRef confidence="0.0" refType="SubClassOf" reference="ExtendedDnS.owl#social-object" resource="" status="implied">
                    <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#non-physical-object" resource="" status="implied">
                        <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#non-physical-endurant" resource="" status="implied">
                            <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#endurant" resource="" status="implied">
                                <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#spatio-temporal-particular" resource="" status="implied"/>
                                <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#particular" resource="" status="implied"/>
                            </externalRef>
                        </externalRef>
                    </externalRef>
                </externalRef>
            </externalRef>
     </externalRef>
</externalRef>
<externalRef confidence="0.0233253" refType="" reference="eng-30-08180190-n" resource="wn30g_eng.v20" status="">
    <externalRef confidence="0.0" refType="base-concept" reference="eng-30-00031264-n" resource="" status=""/>
    <externalRef confidence="0.0" refType="sc_subClassOf" reference="Kyoto#group__grouping-eng-3.0-00031264-n" resource="" status="">
        <externalRef confidence="0.0" refType="SubClassOf" reference="ExtendedDnS.owl#non-agentive-social-object" resource="" status="implied">
            <externalRef confidence="0.0" refType="SubClassOf" reference="ExtendedDnS.owl#social-object" resource="" status="implied">
                <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#non-physical-object" resource="" status="implied">
                    <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#non-physical-endurant" resource="" status="implied">
                        <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#endurant" resource="" status="implied">
                            <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#spatio-temporal-particular" resource="" status="implied"/>
                            <externalRef confidence="0.0" refType="SubClassOf" reference="DOLCE-Lite.owl#particular" resource="" status="implied"/>
                        </externalRef>
                    </externalRef>
                </externalRef>
            </externalRef>
        </externalRef>
    </externalRef>
</externalRef>
</externalReferences>
</term>

2. Tag a folder with KAF files with base-concepts and ontology-labels

Class:  eu.kyotoproject.main.KafOntotaggerFolder

Usage:
parameters:
--input-folder          <path to the folder with the kaf files>
--extension             <extension of the file name to be processed, e.g. ".kaf">
--synset-baseconcept    [optional] <path to a text file with synset to base concept mappings>
--synset-ontology       [optional] <path to a text file with synset to ontology label mappings>
--ontology-ontology     [optional] <path to the explicit ontology relations, assumes --synset-ontology>
--relations             [optional] <path to a text file with the relations accepted>
--format                <values "naf" or "kaf": indicate the format of the output>

This function works the same as 1. except that the output is stored in a file withe the name of the input KAF with
extended with ".onto.kaf".


3. Tag verbs in a KAF file with SemLink types

Class:  eu.kyotoproject.main.KafPredicateMatrixTagger

Usage:
parameters:
--kaf-file or --naf-file <path to the n/kaf file>
--pos                    [optional] <part-of-speech of the term to be considered should start with the value of pos>
--predicate-matrix       <path to a text file with predicate-matrix>
--version                [optional] <version label of the predicate-matrix>
--key                    [optional] <prefix for the column from the predicate-matrix that holds the synset identifier
--ili                    [optional] <if present, only ili-records are matched with the senses in the term layer>
--ignore-prefix          [optional] <if synset identifiers in naf/kaf are NOT prefixed with 3-letter language code and version but the predicate matrix is, you can here specify the prefix for the predicate matrix to be ignore>
--format                 <values "naf" or "kaf": indicate the format of the output>
--mappings               [optional] <string with the prefixes of the coolumns that should be added, each prefix separated by ";", e.g. "fn;mcr;vn">
--grammatical-words      [optional] <file with stop words that should not be tagged>

Output:

The next example shows the output for the verb die, which has 11 meaning in WordNet. Two meanings have information
in the predicate-matrix file, where:

vn = VerbNet
fn = FrameNet
pb = PropBank
mcr = Multilingual Central Repository
wn = WordNet


<term lemma="die" pos="VBD" tid="t3" type="open">
<span>
<target id="w3"/>
</span>
<externalReferences>
<externalRef confidence="0.662059" refType="" reference="eng-30-00358431-v" resource="wn30g_eng.v20" status="">
<externalRef confidence="0.0" refType="" reference="vn:48.2" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:die" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="wn:die%2:30:00" resource="wn" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:Patient" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:Death" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:924" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:Protagonist" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:die.01" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:1" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:ili-30-00358431-v" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:medicine" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:Death" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:change" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:Initial_Location" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:expire" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="wn:expire%2:30:00" resource="wn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:3990" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:expire.01" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:perish" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="wn:perish%2:30:00" resource="wn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:933" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:perish.01" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:0" resource="pb" status=""/>
</externalRef>
<externalRef confidence="0.170929" refType="" reference="eng-30-00434374-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0427208" refType="" reference="eng-30-02109818-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0304736" refType="" reference="eng-30-01074914-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0225363" refType="" reference="eng-30-01784953-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0186228" refType="" reference="eng-30-00354845-v" resource="wn30g_eng.v20" status="">
<externalRef confidence="0.0" refType="" reference="vn:48.2" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:die" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="wn:die%2:30:01" resource="wn" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:Patient" resource="vn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:Death" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:924" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="fn:Protagonist" resource="fn" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:die.01" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="pb:1" resource="pb" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:ili-30-00354845-v" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:factotum" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:Process" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="mcr:change" resource="mc" status=""/>
<externalRef confidence="0.0" refType="" reference="vn:Initial_Location" resource="vn" status=""/>
</externalRef>
<externalRef confidence="0.0171071" refType="" reference="eng-30-01555034-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0107676" refType="" reference="eng-30-01785242-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.00885031" refType="" reference="eng-30-00538323-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.00874009" refType="" reference="eng-30-00224295-v" resource="wn30g_eng.v20" status=""/>
<externalRef confidence="0.0071936" refType="" reference="eng-30-01829475-v" resource="wn30g_eng.v20" status=""/>
</externalReferences>
</term>

4. Tag a folder with KAF files with base-concepts and ontology-labels

Class:  eu.kyotoproject.main.KafPredicateMatrixTaggerFolder

Usage:
parameters:
--input-folder          <path to the folder with the kaf files>
--extension             <extension of the file name to be processed, e.g. ".kaf">
--pos                   [optional] <part-of-speech of the term to be considered should start with the value of pos>
--predicate-matrix      <path to a text file with predicate-matrix>
--version               [optional] <version label of the predicate-matrix>
--key                    [optional] <prefix for the column from the predicate-matrix that holds the synset identifier
--ili                    [optional] <if present, only ili-records are matched with the senses in the term layer>
--format                 <values "naf" or "kaf": indicate the format of the output>
--mappings               [optional] <string with the prefixes of the coolumns that should be added, each prefix separated by ";", e.g. "fn;mcr;vn">
--grammatical-words      [optional] <file with stop words that should not be tagged>


5. Tag lemmas in a KAF file with the classification in an RDF ontology

Class:  eu.kyotoproject.main.KafOntotaggerRdf

Usage:

parameters:
--kaf-file                      <path to the kaf file>
--eu.kyotoproject.rdf-file      <path to an ontology in RDF>

Output:

Example

    <term lemma="nevenaltaar" morphofeat="nounsg unknown_lemma" pos="O" tid="t_1" type="open">
      <span>
        <!--nevenaltaar-->
        <target id="w_1"/>
      </span>
      <externalReferences>
        <externalRef confidence="0.0" refType="" reference="nevenaltaar" resource="" status="">
          <externalRef confidence="0.0" refType="" reference="altaar" resource="" status=""/>
          <externalRef confidence="0.0" refType="" reference="" resource="" status=""/>
        </externalRef>
      </externalReferences>
    </term>
    <term lemma="kinderalbe" morphofeat="nounsg unknown_lemma" pos="O" tid="t_2" type="open">
      <span>
        <!--kinderalbe-->
        <target id="w_2"/>
      </span>
      <externalReferences>
        <externalRef confidence="0.0" refType="" reference="kinderalbe" resource="" status="">
          <externalRef confidence="0.0" refType="" reference="albe" resource="" status=""/>
          <externalRef confidence="0.0" refType="" reference="" resource="" status=""/>
        </externalRef>
      </externalReferences>
    </term>
    <term lemma="communieluikantependium" morphofeat="nounsg unknown_lemma" pos="O" tid="t_3" type="open">
      <span>
        <!--communieluikantependium-->
        <target id="w_3"/>
      </span>
      <externalReferences>
        <externalRef confidence="0.0" refType="" reference="communieluikantependium" resource="" status="">
          <externalRef confidence="0.0" refType="" reference="antependium" resource="" status=""/>
          <externalRef confidence="0.0" refType="" reference="Kerkobject" resource="" status=""/>
        </externalRef>
      </externalReferences>
    </term>
    
