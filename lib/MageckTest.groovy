//
// Groovy helper functions for running MAGeCK Test
//

@Grab(group='org.codehaus.groovy', module='groovy-yaml', version='3.0.16')

import groovy.yaml.YamlSlurper


class MageckTest {
    /**
    * Create a list of Maps containing the values needed to run MAGeCK test for
    * each contrast.
    */
    public static List createContrasts(sampleMeta){
        def samples = new YamlSlurper().parse(sampleMeta)
        
        def samplesByGroup = samples.collectEntries { key, value ->
            [key, [group: value.group, reference: value.reference, is_ref: value.is_ref]]
        }
        .groupBy {it.value.group}

        // In case the samples do not contain a reference key defining the reference
        // groups, find the reference group with the is_ref key (ie, the group that
        // has a sample with is_ref set to 1)
        // TODO: Deprecate the is_ref approach to the reference group in the metadata
        def refGroup = samplesByGroup.findAll{it.value.any{it.value.is_ref}}*.key[0]

        samplesByGroup.collectMany { group, groupSamples ->
            // For this group, get a map of the analyses and corresponding reference groups
            def analysisRefs = groupSamples.collect { _, value ->
                if (!value.reference) {
                    // TODO: Remove this after is_ref usage is deprecated
                    value.reference = ['': (group != refGroup ? refGroup : null)]
                }
                // Analyses with no reference value should be ignored (either this
                // group is not part of that analysis or it is the reference group)
                value.reference.findAll {it.value != null}
            }
            .inject { a, b -> a + b }

            // For each analysis, create a map with the inputs needed for running MAGeCK
            // test to contrast this group to the appropriate reference control group
            analysisRefs.collect { analysisName, referenceGroup ->
                [
                    group: group,
                    samples: groupSamples.keySet(),
                    refGroup: referenceGroup,
                    refSamples: samplesByGroup[referenceGroup].keySet(),
                    analysis: analysisName,
                    contrast: "${group}.vs.${referenceGroup}"
                ]
            }
        }
    }

    public static List createContrasts2(metadata) {
        def samples = new YamlSlurper().parse(metadata).samples
        
        def samplesByGroup = samples.collectEntries { key, value ->
            [value.group_rep, [group: value.group, reference: value.reference, is_ref: value.is_ref]]
        }
        .groupBy {it.value.group}

        // In case the samples do not contain a reference key defining the reference
        // groups, find the reference group with the is_ref key (ie, the group that
        // has a sample with is_ref set to 1)
        // TODO: Deprecate the is_ref approach to the reference group in the metadata
        def refGroup = samplesByGroup.findAll{it.value.any{it.value.is_ref}}*.key[0]

        samplesByGroup.collectMany { group, groupSamples ->
            // For this group, get a map of the analyses and corresponding reference groups
            def analysisRefs = groupSamples.collect { _, value ->
                if (!value.reference) {
                    // TODO: Remove this after is_ref usage is deprecated
                    value.reference = ['': (group != refGroup ? refGroup : null)]
                }
                // Analyses with no reference value should be ignored (either this
                // group is not part of that analysis or it is the reference group)
                value.reference.findAll {it.value != null}
            }
            .inject { a, b -> a + b }

            // For each analysis, create a map with the inputs needed for running MAGeCK
            // test to contrast this group to the appropriate reference control group
            analysisRefs.collect { analysisName, referenceGroup ->
                [
                    group: group,
                    samples: groupSamples.keySet(),
                    refGroup: referenceGroup,
                    refSamples: samplesByGroup[referenceGroup].keySet(),
                    analysis: analysisName,
                    name: "${group}.vs.${referenceGroup}"
                ]
            }
        }
    }
}