======
PV2sim
======

Phenopacket and VCF for simulation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This app creates simulation files for Exomiser to test performance or demonstrate features.


Input files
===========

PV2Sim uses `Phenopackets <https://phenopackets-schema.readthedocs.io/en/latest/>`_ as
the basis for simulated cases. The Phenopacket should contain a list of
`Human Phenotype Ontology <https://hpo.jax.org/app/>`_ terms, a disease diagnosis, and
one or two variants deemed to be disease-causing.

PV2Sim needs three input files

* The phenopacket (in JSON format)
* hp.obo
* a template VCF file (usually a file that does not contain a pathogenic variant)


What PV2SIM does
================

PV2Sim extracts the data from the Phenopacket, and creates an input YAML file for Exomiser. It also
extracts the variants from the Phenopacket and *spikes* them into the VCF file.

Output files
============

PV2Sim outputs the simulated VCF file (with the disease associated variants) and the YAML file for Exomiser.

Building and running PV2Sim
===========================

To build the app, clone this repository and use maven to generate the app. ::

    $ git clone https://github.com/pnrobinson/PV2sim.git
    $ cd PV2Sim
    $ mvn package

The run the app, enter (at least) the following data. ::

    $ java -jar pv2sim.jar -v template.vcf -p phenopacket.json -h hp.obo

Options
=======

PVSim can adjust the Exomiser parameters (i.e., the settings in the YAML file) with the following flags. ::

  Options:
    -f, --frequency-threshold
      Exomiser frequency threshold
      Default: 0.1
    --help
      display this help message
  * -h, --hpo
      path to hp.obo
    -k, --keepNonPatho
      Keep non pathogenic vars (Exomiser)
      Default: true
    -o, --outputContributingVariantsOnly
      output Contributing Variants Only (Exomiser
      Default: false
  * -p, --phenopacket
      path to phenopacket
    -x, --prefix
      outputfile prefix
      Default: pv2sim
  * -v, --vcf
      path to VCF file
    --vcf-out
      name/path of simulated (output) VCF file
      Default: pv2sim-out.vcf
    -y, --yaml
      path of output YAML file
      Default: pv2sim.yml

