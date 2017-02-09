/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.data.internal.controller;

import org.phenotips.Constants;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.SimpleValuePatientData;
import org.phenotips.data.internal.PhenoTipsGene;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.StringListProperty;

/**
 * Handles the patients genes.
 *
 * @version $Id$
 * @since 1.0RC1
 */
@Component(roles = { PatientDataController.class })
@Named("gene")
@Singleton
public class GeneListController implements PatientDataController<List<PhenoTipsGene>>
{
    /** The XClass used for storing gene data. */
    protected static final EntityReference GENE_CLASS_REFERENCE = new EntityReference("GeneClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    private static final String GENES_STRING = "genes";

    private static final String CONTROLLER_NAME = GENES_STRING;

    private static final String GENES_ENABLING_FIELD_NAME = GENES_STRING;

    private static final String INTERNAL_GENE_KEY = "gene";

    private static final String INTERNAL_STATUS_KEY = "status";

    private static final String INTERNAL_STRATEGY_KEY = "strategy";

    private static final String INTERNAL_COMMENTS_KEY = "comments";

    private static final String INTERNAL_CANDIDATE_VALUE = "candidate";

    private static final String INTERNAL_REJECTED_VALUE = "rejected";

    private static final String INTERNAL_SOLVED_VALUE = "solved";

    private static final String JSON_GENE_ID = "id";

    private static final String JSON_GENE_SYMBOL = INTERNAL_GENE_KEY;

    private static final String JSON_DEPRECATED_GENE_ID = JSON_GENE_SYMBOL;

    private static final String JSON_SOLVED_KEY = INTERNAL_SOLVED_VALUE;

    private static final String JSON_REJECTEDGENES_KEY = "rejectedGenes";

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private Logger logger;

    /** Provides access to the current execution context. */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public String getName()
    {
        return CONTROLLER_NAME;
    }

    @Override
    public SimpleValuePatientData<List<PhenoTipsGene>> load(Patient patient)
    {
        try {
            XWikiDocument doc = (XWikiDocument) this.documentAccessBridge.getDocument(patient.getDocument());
            List<BaseObject> geneXWikiObjects = doc.getXObjects(GENE_CLASS_REFERENCE);
            if (geneXWikiObjects == null || geneXWikiObjects.isEmpty()) {
                return null;
            }

            List<PhenoTipsGene> allGenes = new LinkedList<>();
            for (BaseObject geneObject : geneXWikiObjects) {
                if (geneObject == null || geneObject.getFieldList().isEmpty()) {
                    continue;
                }

                String id = getFieldValue(geneObject, INTERNAL_GENE_KEY);
                String status = getFieldValue(geneObject, INTERNAL_STATUS_KEY);
                String strategy = getFieldValue(geneObject, INTERNAL_STRATEGY_KEY);
                String comment = getFieldValue(geneObject, INTERNAL_COMMENTS_KEY);

                PhenoTipsGene gene = new PhenoTipsGene(id, null, status, strategy, comment);

                allGenes.add(gene);
            }
            if (allGenes.isEmpty()) {
                return null;
            } else {
                return new SimpleValuePatientData<>(getName(), allGenes);
            }
        } catch (Exception e) {
            this.logger.error("Could not find requested document or some unforeseen "
                + "error has occurred during controller loading ", e.getMessage());
        }
        return null;
    }

    private String getFieldValue(BaseObject geneObject, String property)
    {
        if (INTERNAL_STRATEGY_KEY.equals(property)) {
            StringListProperty fields = (StringListProperty) geneObject.getField(property);
            if (fields == null || fields.getList().size() == 0) {
                return null;
            }
            return fields.getTextValue();

        } else {
            BaseStringProperty field = (BaseStringProperty) geneObject.getField(property);
            if (field == null) {
                return null;
            }
            return field.getValue();
        }
    }

    @Override
    public void writeJSON(Patient patient, JSONObject json, Collection<String> selectedFieldNames)
    {
        if (selectedFieldNames != null && !selectedFieldNames.contains(GENES_ENABLING_FIELD_NAME)) {
            return;
        }

        PatientData<List<PhenoTipsGene>> data = patient.getData(getName());
        if (data == null || data.getValue() == null || data.getValue().isEmpty()) {
            if (selectedFieldNames == null || selectedFieldNames.contains(GENES_ENABLING_FIELD_NAME)) {
                json.put(GENES_STRING, new JSONArray());
            }
            return;
        }

        List<PhenoTipsGene> genes = data.getValue();
        JSONArray geneArray = new JSONArray();

        for (PhenoTipsGene gene : genes) {
            geneArray.put(gene.toJSON());
        }

        json.put(GENES_STRING, geneArray);
    }

    @Override
    public PatientData<List<PhenoTipsGene>> readJSON(JSONObject json)
    {
        if (json == null
            || !(json.has(GENES_STRING) || json.has(JSON_SOLVED_KEY) || json.has(JSON_REJECTEDGENES_KEY))) {
            return null;
        }

        try {
            List<PhenoTipsGene> accumulatedGenes = new LinkedList<>();

            parseGenesJson(json, accumulatedGenes);

            // v1.2.x json compatibility
            parseRejectedGenes(json, accumulatedGenes);
            parseSolvedGene(json, accumulatedGenes);

            return new SimpleValuePatientData<>(getName(), accumulatedGenes);
        } catch (Exception e) {
            this.logger.error("Could not load genes from JSON: [{}]", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Supports both 1.3-m5 and older 1.3-xx format. 1.3-m5 and newer format: {"id": ENSEMBL_Id [[, "gene": HGNC_Symbol]
     * , ...] } 1.3-old format: {"gene": HGNC_Symbol [, ...] }
     */
    private void parseGenesJson(JSONObject json, List<PhenoTipsGene> accumulatedGenes)
    {
        JSONArray genesJson = json.optJSONArray(GENES_STRING);

        Set<String> alreadyCollectedGeneNames = new HashSet<>();

        if (genesJson != null) {
            for (int i = 0; i < genesJson.length(); ++i) {
                JSONObject geneJson = genesJson.getJSONObject(i);

                // gene ID is either the "id" field, or, if missing, the "gene" field
                // if both missing, gene is not created
                if (StringUtils.isBlank(geneJson.optString(JSON_GENE_ID))
                    && StringUtils.isBlank(geneJson.optString(JSON_GENE_SYMBOL))) {
                    continue;
                }

                PhenoTipsGene gene = new PhenoTipsGene(geneJson);
                if (gene == null || alreadyCollectedGeneNames.contains(gene.getId())) {
                    continue;
                }

                accumulatedGenes.add(gene);
                alreadyCollectedGeneNames.add(gene.getId());
            }
        }
    }

    private void parseRejectedGenes(JSONObject json, List<PhenoTipsGene> accumulatedGenes)
    {
        Set<String> rejectedGeneNames = new HashSet<>();

        JSONArray rejectedGenes = json.optJSONArray(JSON_REJECTEDGENES_KEY);
        if (rejectedGenes != null && rejectedGenes.length() > 0) {
            for (int i = 0; i < rejectedGenes.length(); ++i) {
                JSONObject rejectedGeneJson = rejectedGenes.getJSONObject(i);

                // discard it if gene symbol is blank or empty
                if (StringUtils.isBlank(rejectedGeneJson.optString(JSON_DEPRECATED_GENE_ID))) {
                    continue;
                }

                PhenoTipsGene gene = new PhenoTipsGene(rejectedGeneJson);
                if (gene == null || rejectedGeneNames.contains(gene.getId())) {
                    continue;
                }

                gene.setStatus(INTERNAL_REJECTED_VALUE);

                // overwrite the same gene if it was found to be as candidate
                addOrReplaceGene(accumulatedGenes, gene);

                rejectedGeneNames.add(gene.getId());
            }
        }
    }

    private void parseSolvedGene(JSONObject json, List<PhenoTipsGene> accumulatedGenes)
    {
        JSONObject solvedGene = json.optJSONObject(JSON_SOLVED_KEY);
        if (solvedGene == null) {
            return;
        }

        // discard it if gene symbol is blank or empty
        if (StringUtils.isBlank(solvedGene.optString(JSON_DEPRECATED_GENE_ID))) {
            return;
        }

        PhenoTipsGene gene = new PhenoTipsGene(solvedGene);

        gene.setStatus(INTERNAL_SOLVED_VALUE);

        // overwrite the same gene if it was found to be a candidate or rejected
        addOrReplaceGene(accumulatedGenes, gene);
    }

    private void addOrReplaceGene(List<PhenoTipsGene> allGenes, PhenoTipsGene gene)
    {
        // need index for replacement; performance is not critical since this code is only
        // used for old 1.2.x. patient JSONs
        for (int i = 0; i < allGenes.size(); i++) {
            if (StringUtils.equals(allGenes.get(i).getId(), gene.getId())) {
                allGenes.set(i, gene);
                return;
            }
        }
        allGenes.add(gene);
    }

    @Override
    public void save(Patient patient, DocumentModelBridge doc)
    {
        PatientData<List<PhenoTipsGene>> data = patient.getData(this.getName());
        if (data == null) {
            return;
        }

        XWikiContext context = this.xcontextProvider.get();
        ((XWikiDocument) doc).removeXObjects(GENE_CLASS_REFERENCE);

        List<PhenoTipsGene> genes = data.getValue();
        if (genes == null || genes.isEmpty()) {
            return;
        }

        for (PhenoTipsGene gene : genes) {
            try {
                BaseObject xwikiObject = ((XWikiDocument) doc).newXObject(GENE_CLASS_REFERENCE, context);

                setXwikiObjectProperty(INTERNAL_GENE_KEY, gene.getName(), xwikiObject, context);
                String status = gene.getStatus();
                // setting status to default 'candidate' if not defined yet
                setXwikiObjectProperty(INTERNAL_STATUS_KEY,
                    StringUtils.isBlank(status) ? status : INTERNAL_CANDIDATE_VALUE, xwikiObject, context);
                setXwikiObjectProperty(INTERNAL_STRATEGY_KEY, gene.getStrategy(), xwikiObject, context);
                setXwikiObjectProperty(INTERNAL_COMMENTS_KEY, gene.getComment(), xwikiObject, context);
            } catch (Exception e) {
                this.logger.error("Failed to save a specific gene: [{}]", e.getMessage());
            }
        }
    }

    private void setXwikiObjectProperty(String property, String value, BaseObject xwikiObject, XWikiContext context)
    {
        if (value != null) {
            xwikiObject.set(property, value, context);
        }
    }

    @Override
    public void writeJSON(Patient patient, JSONObject json)
    {
        writeJSON(patient, json, null);

    }
}
