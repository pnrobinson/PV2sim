package org.monarchinitiative.yaml;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class Yamlator {

    private final String genomeAssembly;
    private final String vcf;
    private final String proband;
    private List<String> hpoIds;
    private final String outputprefix;
    private final double frequency;
    // HTML, JSON, TSV_GENE, TSV_VARIANT, VCF
    private final List<String> outputformats;
    private final boolean keepNonPatho;
    private final boolean outputContributingVariantsOnly;

    private final static String fourSpaces = "    ";


    public Yamlator(Builder builder) {
        this.genomeAssembly = builder.genomeAssembly;
        this.vcf = builder.vcf;
        this.proband = builder.proband;
        this.hpoIds = builder.hpoIds;
        this.outputprefix = builder.outputprefix;
        this.frequency=builder.frequency;
        this.outputformats=builder.outputformats;
        this.keepNonPatho=builder.keepNonPatho;
        this.outputContributingVariantsOnly=builder.outputContributingVariantsOnly;
    }

    private String header = "## Exomiser Analysis Template.\n" +
            "# Generated by PV2Sim.\n" +
            "---\n" +
            "analysis:";

    private String frequencySources[] = {
            "THOUSAND_GENOMES",
            "TOPMED",
            "UK10K",
            "ESP_AFRICAN_AMERICAN",
            "ESP_EUROPEAN_AMERICAN",
            "ESP_ALL",
            "EXAC_AFRICAN_INC_AFRICAN_AMERICAN",
            "EXAC_AMERICAN",
            "EXAC_SOUTH_ASIAN",
            "EXAC_EAST_ASIAN",
            "EXAC_FINNISH",
            "EXAC_NON_FINNISH_EUROPEAN",
            "EXAC_OTHER",
            "GNOMAD_E_AFR",
            "GNOMAD_E_AMR",
            "GNOMAD_E_EAS",
            "GNOMAD_E_FIN",
            "GNOMAD_E_NFE",
            "GNOMAD_E_OTH",
            "GNOMAD_E_SAS",
            "GNOMAD_G_AFR",
            "GNOMAD_G_AMR",
            "GNOMAD_G_EAS",
            "GNOMAD_G_FIN",
            "GNOMAD_G_NFE",
            "GNOMAD_G_OTH",
            "GNOMAD_G_SAS"
    };

    private String removeSteps[] = {
            "FIVE_PRIME_UTR_EXON_VARIANT",
            "FIVE_PRIME_UTR_INTRON_VARIANT",
            "THREE_PRIME_UTR_EXON_VARIANT",
            "THREE_PRIME_UTR_INTRON_VARIANT",
            "NON_CODING_TRANSCRIPT_EXON_VARIANT",
            "UPSTREAM_GENE_VARIANT",
            "INTERGENIC_VARIANT",
            "REGULATORY_REGION_VARIANT",
            "CODING_TRANSCRIPT_INTRON_VARIANT",
            "NON_CODING_TRANSCRIPT_INTRON_VARIANT",
            "DOWNSTREAM_GENE_VARIANT"
    };


    public void write(Writer w) throws IOException {
        w.write(header + "\n");
        w.write(fourSpaces + "genomeAssembly: " + genomeAssembly + "\n");
        w.write(fourSpaces + "vcf: " + vcf + "\n");
        w.write(fourSpaces + "ped:\n");
        w.write(fourSpaces + "proband: " + proband + "\n");
        String result = hpoIds.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        w.write(fourSpaces + "hpoIds: [" + result + "]\n");
        w.write(fourSpaces + "inheritanceModes: {}\n");
        w.write(fourSpaces + "analysisMode: PASS_ONLY\n");
        result = String.join(", ", frequencySources);
        w.write(fourSpaces + "frequencySources: [" + result + "]\n");
        w.write(fourSpaces + "pathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT]\n");
        w.write(fourSpaces + "steps: [\n");
        w.write(fourSpaces + fourSpaces + "variantEffectFilter: {\n");
        result = String.join(", ", removeSteps);
        w.write(fourSpaces + fourSpaces + fourSpaces + "remove: [" + result + "]\n");
        w.write(fourSpaces + fourSpaces + "},\n");
        w.write(fourSpaces + fourSpaces + "frequencyFilter: {maxFrequency: " + frequency+ "},\n");
        w.write(fourSpaces + fourSpaces + "pathogenicityFilter: {keepNonPathogenic: "+keepNonPatho+"},\n");
        w.write(fourSpaces + fourSpaces + "inheritanceFilter: {},\n");
        w.write(fourSpaces + fourSpaces + "omimPrioritiser: {},\n");
        w.write(fourSpaces + fourSpaces + "hiPhivePrioritiser: {},\n");
        w.write(fourSpaces + "]\n");
        w.write("outputOptions:\n");
        w.write(fourSpaces + "outputContributingVariantsOnly: "+ outputContributingVariantsOnly+"\n");
        w.write(fourSpaces + "numGenes: 0\n");
        w.write(fourSpaces + "outputPrefix: " + this.outputprefix+"\n");
        w.write(fourSpaces + "outputFormats: [" + String.join(",",outputformats)+"]\n");
    }

    public static class Builder {
        private  String genomeAssembly;
        private  String vcf;
        private  String proband;
        private List<String> hpoIds;
        private  String outputprefix;
        private double frequency=0.1;//default 0.1% threshold
        // HTML, JSON, TSV_GENE, TSV_VARIANT, VCF
        private List<String> outputformats=ImmutableList.of("HTML","TSV_VARIANT");
        private boolean keepNonPatho=true;
        private boolean outputContributingVariantsOnly= false;

        public Builder(String genomeAssem,
                 String vcf,
                 String proband,
                 List<String> hpos,
                 String output) {
            this.genomeAssembly = genomeAssem;
            this.vcf = vcf;
            this.proband = proband;
            this.hpoIds = hpos;
            this.outputprefix = output;
        }

        public Builder frequencyThreshold(double f) { frequency=f; return this;}
        public Builder outputFormats(List<String> L) { this.outputformats=L;return this;}
        public Builder keepNonPathogenic(boolean b) { this.keepNonPatho=b; return this;}
        public Builder outputContributingVariantsOnly(boolean b) { this.outputContributingVariantsOnly=b;return this;}

        public Yamlator build() {
            return new Yamlator(this);
        }

    }

}
