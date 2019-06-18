package org.monarchinitiative;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket.PhenopacketImporter;
import org.monarchinitiative.vcf.VcfSimulator;
import org.monarchinitiative.yaml.Yamlator;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenopacket.PhenopacketImporter.fromJson;

/**
 * Hello world!
 */
public class PV2Sim {
    private static final Logger logger = LoggerFactory.getLogger(PhenopacketImporter.class);
    @Parameter(names = {"-p", "--phenopacket"}, description = "path to phenopacket", required = true)
    private String phenopacketPath;
    @Parameter(names = {"-v", "--vcf"}, description = "path to VCF file", required = true)
    private String vcfPath;
    @Parameter(names = {"-h", "--hpo"}, description = "path to hp.obo", required = true)
    private String ontologyPath;
    @Parameter(names = {"-x", "--prefix"}, description = "outputfile prefix")
    private String prefix = "pv2sim";
    @Parameter(names = {"-y", "--yaml"}, description = "path of output YAML file")
    private String yamlPath = "pv2sim.yml";
    @Parameter(names = {"--vcf-out"}, description = "name/path of simulated (output) VCF file")
    private String vcfout = "pv2sim-out.vcf";
    @Parameter(names = {"-f", "--frequency-threshold"}, description = "Exomiser frequency threshold")
    private float frequency = 0.1f;
    @Parameter(names = {"-k", "--keepNonPatho"}, description = "Keep non pathogenic vars (Exomiser)")
    boolean keepNonPatho = true;
    @Parameter(names = {"-o", "--outputContributingVariantsOnly"}, description = "output Contributing Variants Only (Exomiser")
    boolean outputContributingVariantsOnly = false;
    @Parameter(names = {"--help"}, help = true, arity = 0, description = "display this help message")
    private boolean usageHelpRequested;

    private Ontology hpo;
    /**
     * The id of the correct diagnosis (should be of the form OMIM:600123).
     */
    private TermId diseaseId;

    private List<TermId> observedHpoList;

    private List<TermId> excludedHpoList;
    /**
     * The NCBI Entrez Id of the gene with the causative variant(s).
     */
    private TermId geneId;

    // This controls what kinds of file the Exomiser will output
    // options are HTML, JSON, TSV_GENE, TSV_VARIANT, VCF
    private List<String> outputFormats = ImmutableList.of("HTML", "TSV_VARIANT");


    public static void main(String[] args) {
        PV2Sim pv2sim = new PV2Sim();
        JCommander jc = JCommander.newBuilder()
                .addObject(pv2sim).build();
        jc.parse(args);
        if (pv2sim.usageHelpRequested) {
            jc.usage();
            System.exit(1);
        }
        pv2sim.run();
    }


    private PV2Sim() {
    }

    private void run() {
        System.out.println("[INFO] phenopacket path = " + phenopacketPath);
        System.out.println("[INFO] vcf path = " + vcfPath);
        System.out.println("[INFO] hpo path=" + ontologyPath);
        // first get the phenotype information
        this.hpo = OntologyLoader.loadOntology(new File(ontologyPath));
        PhenopacketImporter phenopacketImporter = fromJson(this.phenopacketPath, hpo);
        this.diseaseId = TermId.of(phenopacketImporter.getDiagnosis().getTerm().getId());
        this.observedHpoList = phenopacketImporter.getHpoTerms();
        this.excludedHpoList = phenopacketImporter.getNegatedHpoTerms();
        List<Variant> variantList = phenopacketImporter.getVariantList();
        String sampleName = phenopacketImporter.getSamplename();
        String genomeAssembly = phenopacketImporter.getGenomeAssembly();
        // Now add the variants to the VCF file.
        // first check validity of inputs
        if (genomeAssembly == null) {
            logger.error("genomeAssembly missing from " + this.phenopacketPath);
            System.exit(1);
        }
        if (diseaseId == null) {
            logger.error("diseaseId missing from " + this.phenopacketPath);
            System.exit(1);
        }


        VcfSimulator vcfSimulator = new VcfSimulator(Paths.get(this.vcfPath), this.vcfout);
        HtsFile simulatedVcf;
        try {
            simulatedVcf = vcfSimulator.simulateVcf(sampleName, variantList, genomeAssembly);
        } catch (IOException e) {
            throw new RuntimeException("Could not simulate VCF for phenopacket");
        }
        if (simulatedVcf == null) {
            System.err.println("[ERROR] Could not simulate VCF for " + phenopacketPath); // should never happen
            System.exit(1);
        }
        String vcfPath = simulatedVcf.getFile().getPath();
        System.out.println("[INFO] created simulated VCF file at" + vcfPath);
        // create YAML file
        List<String> hposAsStringList = observedHpoList.stream().
                map(TermId::getValue).collect(Collectors.toList());

        Yamlator.Builder builder = new Yamlator.Builder(genomeAssembly,
                vcfPath,
                sampleName,
                hposAsStringList,
                prefix).frequencyThreshold(this.frequency)
                .keepNonPathogenic(this.keepNonPatho)
                .outputContributingVariantsOnly(this.outputContributingVariantsOnly)
                .outputFormats(this.outputFormats);

        Yamlator yaml = builder.build();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.yamlPath));
            yaml.write(bw);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
