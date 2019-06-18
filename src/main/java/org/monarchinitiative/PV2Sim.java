package org.monarchinitiative;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenopacket.PhenopacketImporter;
import org.monarchinitiative.vcf.VcfSimulator;
import org.monarchinitiative.yaml.Yamlator;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Variant;

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
 *
 */
public class PV2Sim
{
    @Parameter(names={"-p","--phenopacket"},description="path to phenopacket",required = true)
    private String phenopacketPath;
    @Parameter(names={"-v","--vcf"},description = "path to VCF file",required = true)
    private String vcfPath;
    @Parameter(names={"-h","--hpo"},description = "path to hp.obo",required = true)
    private String ontologyPath;
    @Parameter(names={"-x","--prefix"},description = "outputfile prefix")
    private String prefix = "pv2sim";
    @Parameter(names={"-y","--yaml"},description = "path of output YAML file")
    private String yamlPath="pv2sim.yml";

    private Ontology hpo;
    /** The id of the correct diagnosis (should be of the form OMIM:600123). */
    private TermId diseaseId;

    private List<TermId> observedHpoList;

    private List<TermId> excludedHpoList;
    /** The NCBI Entrez Id of the gene with the causative variant(s). */
    private TermId geneId;








    public static void main( String[] args )
    {
        PV2Sim pv2sim = new PV2Sim();
        JCommander.newBuilder()
                .addObject(pv2sim)
                .build()
                .parse(args);

        pv2sim.run();
    }


    private PV2Sim(){
    }

    private void run() {
        System.out.println( "[INFO] phenopacket path = " + phenopacketPath );
        System.out.println( "[INFO] vcf path = " + vcfPath );
        System.out.println("[INFO] hpo path="+ontologyPath);
        // first get the phenotype information
        this.hpo = OntologyLoader.loadOntology(new File(ontologyPath));
        PhenopacketImporter phenopacketImporter = fromJson(this.phenopacketPath,hpo);
        this.diseaseId = TermId.of(phenopacketImporter.getDiagnosis().getTerm().getId());
        this.observedHpoList=phenopacketImporter.getHpoTerms();
        this.excludedHpoList=phenopacketImporter.getNegatedHpoTerms();
        List<Variant> variantList=phenopacketImporter.getVariantList();
        String sampleName = phenopacketImporter.getSamplename();
        String genomeAssembly = phenopacketImporter.getGenomeAssembly();
        // Now add the variants to the VCF file.
        VcfSimulator vcfSimulator = new VcfSimulator(Paths.get(this.vcfPath));
        HtsFile simulatedVcf;
        try {
            simulatedVcf = vcfSimulator.simulateVcf(sampleName, variantList, genomeAssembly);
        } catch (IOException e) {
            throw new RuntimeException("Could not simulate VCF for phenopacket");
        }
        if (simulatedVcf == null) {
            System.err.println("[ERROR] Could not simulate VCF for "+phenopacketPath); // should never happen
            System.exit(1);
        }
        String vcfPath = simulatedVcf.getFile().getPath();
        System.out.println("[INFO] created simulated VCF file at"+vcfPath);
        // create YAML file
        List<String> hposAsStringList = observedHpoList.stream().
                map(TermId::getValue).collect(Collectors.toList());
        Yamlator yaml = new Yamlator(genomeAssembly,
                    vcfPath,
                    sampleName,
                hposAsStringList,
                    prefix);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.yamlPath));
            yaml.write(bw);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }










}
