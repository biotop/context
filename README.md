# CONTEXT - A TOOL TO ANALYSE SEQUENCE CONTEXT OF GENERIC (BIOLOGICAL) DATA

## Motivation 
The analysis of sequence context presents a common challenge/question in many biological problems. 
- Example 1: [Sequence context around sRNA TSS in B. Burgdorferi](http://www.cibiv.at/~niko/bbdb/pdf/NucleotidProfile201.strandaware.zoom-50_10.pdf)
- Example 2: [Sequence context around concordant/discordant SNV/INDEL calls in human WGS data, see Supplementary data, p22 ff.](https://doi.org/10.1093/bioinformatics/btw587)

## Core Task
Compare features of a sequence window around a site of interest to a suitable set of reference sequences.


## Aim
- Explorative tool to better understand the influence of sequence context on biological functionality/processes and generate hypotheses that can then be verified/falsified using orthogonal approaches.
  - Example: study the influence of sequence context on mutation rates, see, e.g., http://biorxiv.org/content/biorxiv/early/2017/02/14/108290.full.pdf
- Reduce complexity of sequence context analyses, also for non-bioinformaticians
  - E.g. by developing a simple and intuitive GUI and/or by developing a Web-based solution
  - Put the human in the loop – GUI should support exploratory analyses and support community editing/annotation/discussion of input/output data & hypotheses
  - [KISS](https://en.wikipedia.org/wiki/KISS_principle) approach: users should be enabled to recalculate everything easily. CONTEXT should provide full description of how the result data was generated.
- Build a global **community** of sequence context experts


## Implementation
- Plot (strand-specific) sequence profiles (ie. fractions of sequence elements vs. relative position) centered at a set of input positions/intervals
  - Also enable to plot linear combinations of fractions (e.g., “A+T”, “G+C”).
  - Example: This PDF shows nucleotide profile plots in a +/-100bp sequence context around various subsets of small-RNA transcription start sites (sRNA TSS) in borrelia burgdorferii.
One core use case could be that a researcher uploads the position set of all sRNA TSS and CONTEXT then supports sub-setting these positions and creating the shown plots, additionally plotting a background signal (e.g., gene TSS)
  - Enable plotting n background signals, e.g. by random uniform sampling (bootstrapping) from background annotation sets (see below). NOTE that the nucleotide profiles for 
these background annotation sets can be pre-calculated which should enable fast response times.
  - “zoom stage” can be set by user (e.g., “show 1000bp up/downstream”, etc.) but we might have to restrict to a predefined maximum value to make this efficient (nucleotide profiles for the largest possible zoom will be precomputed. Then, an interactive data exploration even in a Browser-based environment should be feasible).
  - Automatically plot genomic average signal (e.g., mean fractions).

- Also support summary statistics calculated from context 
  - Example: test read mapability around a set of found SNVs vs. random SNPs.
    - CONTEXT gets a VCF file and a BAM as user input
    - CONTEXT runs a synchronized iterator over VCF and BAM and calculates the mean mappability from the reads in a genomic context window around each input SNV.
    - CONTEXT does the same for a random subsample of SNPs (NOTE: support caching so users can quickly test against previously evaluated subsets)
    - CONTEXT creates a boxplot that visualizes the mean mappability distributions for input and background sets

- Integrate a plethora existing annotation sets and enable the usage of such annotation sets as background data
  - Examples: exonic, common SNPs; rare DNMs; etc.
  - Basically every annotation set that can, e.g., be downloaded via the UCSC Table Browser may become a background set
  - Users may upload own background annotation sets
  - Tool should provide standard statistics to describe annotation sets (e.g., what % of genome is covered, etc)
  - Annotation data should be normalized
    - Simple input format (e.g., UCSC BED files)
    - Canonically sorted
    - Overlapping vs. non-overlapping intervals in annotation sets?
    - Annotation sets should be as “elementary” as possible. Derivation of meaningful subsets should be automized:
E.g., intersect annotation sets (e.g., human SNP positions +  exon annotations => exonic SNPs).
    - A subtool for preparing annotation sets should be provided.
  - Annotation datasets could be added in a community effort. Users could comment on existing annotation sets but also on calculated sequence context graphs, e.g., linking to existing publications that support/contradict the shown data, etc.
- Iterative approach
  - User input data may become new annotation set
    - users would have to provide a brief description of how the data was created (e.g., the biological experiment)
  - Correlations between annotation sets will be checked automatically (in its simplest form: % overlap. Possibly also develop a similarity measure for sequence profiles)
    - only for primary data (i.e., not derived from existing annotation sets). How to check?
  - User matching based on found correlations:
    - E.g.: “CONTEXT noticed that the dataset ‘small-RNA TSS in Bb’ seems correlated to your dataset ‘B31 sRNA TSS’. Do you want to learn more about this dataset?

- Abstract implementation
  - Implementation should be as abstract as possible (e.g., no “biological knowledge” in the core algorithms!).
  - It should support sequences of any kind (including nucleotide and amino-acid sequences).
    - Support multiple reference genomes (and possibly versions)
    - Start simple: human and e.coli?
  - CONTEXT should also support “translators” that translate given sequences to another representation (possibly using another core alphabet) based on simple rules
    - Example: translate an amino acid sequence to a sequence representing various properties of these AAs (e.g., steric properties, polarity, charge, etc.).
- Don’t do it all. CONTEXT should concentrate only on its core business which is to visualize/identify signal similarities/differences in sequence context data. But CONTEXT may well provide output data in a form that can then be plugged into more specialized tools (e.g, it could create all required files for analysis in MEME/Homer if researches want to look for actual sequence motifs).

## Directions
- **Data-based matching of researchers** - There's no better description of a scientists research interests than her data. Provide a space for researchers to discuss common features based on their data. Open invitation/Matchmaking
- **Provide a playful exploratory tool that invites researchers to look at data from various points of view** - Gamification!?
- **Extend to use any generic sequence data** - Text  to encourage artistic use
- **Great documentation** - Use to write interactive chapter of bioinformatics course
- **Optimisation challenge** - Allow programming challenge-like crowd-sourced optimisation


## Literature
- V. Aggarwala and B. F. Voight. An expanded sequence context model broadly explains variability in polymorphism levels across the human genome. Nat Genet, 48(4):349–355, Apr. 2016 http://www.nature.com/ng/journal/v48/n4/full/ng.3511.html
- A. B. Sahakyan and S. Balasubramanian. Core variability in substitution rates and the basal sequence characteristics of the human genome. http://bmcgenomics.biomedcentral.com/articles/10.1186/s12864-016-3440-5
- Carlson et al., Extremely rare variants reveal patterns of germline mutation rate heterogeneity in humans, https://doi.org/10.1101/108290
- Harpak et al., Mutation Rate Variation is a Primary Determinant of the Distribution of Allele Frequencies in Humans. PLOS genetics. http://journals.plos.org/plosgenetics/article?id=10.1371/journal.pgen.1006489
