package org.monarchinitiative;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

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








    public static void main( String[] args )
    {
        PV2Sim pv2sim = new PV2Sim();
        JCommander.newBuilder()
                .addObject(pv2sim)
                .build()
                .parse(args);

        pv2sim.run();
    }


    public PV2Sim(){
    }

    public void run() {
        System.out.println( "phenopacket path = " + phenopacketPath );
        System.out.println( "vcf path = " + vcfPath );
    }







}
