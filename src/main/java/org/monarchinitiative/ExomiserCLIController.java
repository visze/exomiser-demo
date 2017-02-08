package org.monarchinitiative;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;

import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import de.charite.compbio.jannovar.annotation.VariantEffect;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExomiserCLIController implements ApplicationRunner {

	private final Exomiser exomiser;

	public ExomiserCLIController(Exomiser exomiser) {
		this.exomiser = exomiser;
	}

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		List<String> fileName = applicationArguments.getOptionValues("vcf");
		Path vcfPath = Paths.get(fileName.get(0));

		List<String> hpoIds = applicationArguments.getOptionValues("hpo");

		// do something with this:
		run(vcfPath, hpoIds);

	}

	private AnalysisResults run(Path vcfFile, List<String> hpoIds) {
		Analysis analysis = exomiser.getAnalysisBuilder().vcfPath(vcfFile).hpoIds(hpoIds)
				.frequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES)
				.pathogenicitySources(EnumSet.of(PathogenicitySource.SIFT, PathogenicitySource.POLYPHEN,
						PathogenicitySource.MUTATION_TASTER))
				.addHiPhivePrioritiser(HiPhiveOptions.builder().runParams("human,mouse,fish").build())
				.addPriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f)
				.addVariantEffectFilter(
						EnumSet.of(VariantEffect.UPSTREAM_GENE_VARIANT, VariantEffect.INTERGENIC_VARIANT,
								VariantEffect.DOWNSTREAM_GENE_VARIANT, VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT,
								VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.SYNONYMOUS_VARIANT,
								VariantEffect.SPLICE_REGION_VARIANT, VariantEffect.REGULATORY_REGION_VARIANT))
				.addFrequencyFilter(0.1f).addPathogenicityFilter(true).addOmimPrioritiser().build();
		return exomiser.run(analysis);
	}
}
