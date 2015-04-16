package delfos.group.io.excel.casestudy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.io.excel.casestudy.CaseStudyExcel;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (3-Mayo-2013)
 */
public class GroupCaseStudyExcel {

    public static String RESULT_EXTENSION = "xml";

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;
    public static final String ALL_EXPERIMENTS_SHEET_NAME = CaseStudyExcel.ALL_EXPERIMENTS_SHEET_NAME;
    public static final String CASE_DEFINITION_SHEET_NAME = CaseStudyExcel.CASE_DEFINITION_SHEET_NAME;

    public static final String EXPERIMENT_NAME_COLUMN_NAME = CaseStudyExcel.EXPERIMENT_NAME_COLUMN_NAME;
    public static final String DATASET_LOADER_COLUMN_NAME = CaseStudyExcel.DATASET_LOADER_COLUMN_NAME;

    public static final int EXPERIMENT_NAME_COLUMN = 0;
    public static final int DATASET_LOADER_ALIAS_COLUMN = 1;
    public static final int GROUP_EVALUATION_MEASURES_OFFSET = 2;

    private static WritableCellFormat titleFormat = null;
    private static WritableCellFormat defaultFormat = null;
    private static WritableCellFormat decimalFormat;
    private static WritableCellFormat integerFormat;
    private static final int titleCellWidth = 3 - 1;

    static {
        try {
            initTitleFormat();
            initIntegerFormat();
            initDecimalFormat();
            initDefaultFormat();
        } catch (WriteException ex) {
            Logger.getLogger(GroupCaseStudyExcel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void initTitleFormat() throws WriteException {
        if (titleFormat == null) {
            // create create a bold font with unterlines
            WritableFont times14ptBoldUnderline = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD, false,
                    UnderlineStyle.SINGLE);

            titleFormat = new WritableCellFormat(times14ptBoldUnderline);
            titleFormat.setAlignment(Alignment.CENTRE);

            //Column width control
            titleFormat.setWrap(false);
        }
    }

    public static void initIntegerFormat() {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        integerFormat = new WritableCellFormat(times10pt, new NumberFormat("0"));
    }

    public static void initDecimalFormat() {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        decimalFormat = new WritableCellFormat(times10pt, new NumberFormat("0.00000"));
    }

    public static void initDefaultFormat() throws WriteException {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        defaultFormat = new WritableCellFormat(times10pt);

        //Column width control
        defaultFormat.setWrap(false);

    }

    public static void aggregateExcels(File[] inputFiles, File outputFile) throws WriteException {

        Map<String, Integer> indexColumn = new TreeMap<>();

        Map<String, Map<String, Double>> metricValues_byCase = new TreeMap<>();
        Map<String, Map<String, String>> otherValues_byCase = new TreeMap<>();

        for (File inputFile : inputFiles) {
            try {
                TreeMap<String, Double> valoresDeMetricas = new TreeMap<>();
                Workbook workbook = Workbook.getWorkbook(inputFile);

                Sheet aggregateResults = workbook.getSheet(AGGREGATE_RESULTS);

                for (int columnIndex = 0; columnIndex < aggregateResults.getColumns(); columnIndex++) {
                    String measureName = aggregateResults.getCell(columnIndex, 0).getContents();

                    if (!indexColumn.containsKey(measureName)) {
                        int index = indexColumn.size();
                        indexColumn.put(measureName, index);
                    }
                    CellType type = aggregateResults.getCell(columnIndex, 1).getType();
                    Cell cell = aggregateResults.getCell(columnIndex, 1);

                    String measureValueString = cell.getContents();
                    if (type == CellType.NUMBER) {
                        NumberCell numberRecord = (NumberCell) aggregateResults.getCell(columnIndex, 1);

                        Double measureValue = numberRecord.getValue();
                        valoresDeMetricas.put(measureName, measureValue);
                    } else {
                        throw new IllegalStateException("Cannot recognize cell type");
                    }
                }

                metricValues_byCase.put(inputFile.getName(), valoresDeMetricas);

                otherValues_byCase.put(inputFile.getName(), new TreeMap<>());

                String datasetLoaderName = getConfiguredDatasetLoaderName(workbook.getSheet(CASE_DEFINITION_SHEET_NAME));
                otherValues_byCase.get(inputFile.getName()).put(DATASET_LOADER_COLUMN_NAME, datasetLoaderName);

            } catch (IOException | BiffException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_EXCEL.exit(ex);
            }
        }

        //Escribo resultado.
        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = Workbook.createWorkbook(outputFile, wbSettings);

            if (workbook == null) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + outputFile.getAbsolutePath() + "."));
                return;
            }

            WritableSheet allExperiments = workbook.createSheet(ALL_EXPERIMENTS_SHEET_NAME, 0);
            createLabel(allExperiments);

            //Seet the content.
            int row = 0;

            //Titulos.
            {
                addTitleText(allExperiments, EXPERIMENT_NAME_COLUMN, row, EXPERIMENT_NAME_COLUMN_NAME);
                addTitleText(allExperiments, DATASET_LOADER_ALIAS_COLUMN, row, DATASET_LOADER_COLUMN_NAME);
                for (String metrica : indexColumn.keySet()) {
                    int column = indexColumn.get(metrica);
                    column = column + GROUP_EVALUATION_MEASURES_OFFSET;
                    addTitleText(allExperiments, column, row, metrica);
                }
                row++;
            }

            {
                for (String experimentName : metricValues_byCase.keySet()) {

                    String datasetLoaderName = otherValues_byCase.get(experimentName).get(DATASET_LOADER_COLUMN_NAME);

                    addText(allExperiments, EXPERIMENT_NAME_COLUMN, row, experimentName);

                    addText(allExperiments, DATASET_LOADER_ALIAS_COLUMN, row, datasetLoaderName);

                    Map<String, Double> experimentResults = metricValues_byCase.get(experimentName);
                    for (String metricName : experimentResults.keySet()) {
                        double metricValue = metricValues_byCase.get(experimentName).get(metricName);
                        int column = indexColumn.get(metricName) + GROUP_EVALUATION_MEASURES_OFFSET;
                        addNumber(allExperiments, column, row, metricValue);
                    }
                    row++;
                }
            }

            autoSizeColumns(allExperiments);
            workbook.write();
            workbook.close();

        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }

    }

    private static String getConfiguredDatasetLoaderName(Sheet sheet) {
        String datasetLoaderAlias = null;

        Cell datasetLoaderCell = sheet.findCell(DATASET_LOADER_CELL_CONTENT);
        if (datasetLoaderCell == null) {
            throw new IllegalArgumentException("Cannot datasetLoader cell.");
        }

        Cell datasetLoaderAliasCell = sheet.findCell(
                ParameterOwner.ALIAS.getName(),
                datasetLoaderCell.getColumn(), datasetLoaderCell.getRow(),
                sheet.getColumns(), sheet.getRows(), false);
        if (datasetLoaderAliasCell == null) {
            throw new IllegalArgumentException("Cannot datasetLoader alias cell.");
        }

        Cell dataetLoaderAliasValueCell = sheet.getCell(datasetLoaderAliasCell.getColumn() + 2, datasetLoaderAliasCell.getRow());
        datasetLoaderAlias = dataetLoaderAliasValueCell.getContents();

        if (datasetLoaderAlias == null || datasetLoaderAlias.equals("")) {
            throw new IllegalArgumentException("Cannot find the datasetLoader alias");
        }
        return datasetLoaderAlias;
    }

    private GroupCaseStudyExcel() {
    }

    public synchronized static void saveCaseResults(GroupCaseStudy caseStudyGroup, File file) {

        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = null;

            {
                boolean created = false;
                int i = 0;
                while (!created) {
                    String suffix = "_" + i;
                    File actualFile = FileUtilities.addSufix(file, suffix);
                    if (!actualFile.exists()) {
                        try {
                            workbook = Workbook.createWorkbook(actualFile, wbSettings);
                            created = true;
                        } catch (IOException ex) {
                            created = false;
                        }
                    }
                    i++;
                }
            }
            if (workbook == null) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + file.getAbsolutePath() + "."));
                return;
            }

            WritableSheet caseDefinitionSheet = workbook.createSheet("CaseDefinition", 0);
            createLabel(caseDefinitionSheet);
            createCaseDefinitionSheet(caseStudyGroup, caseDefinitionSheet);
            autoSizeColumns(caseDefinitionSheet);

            WritableSheet executionsSheet = workbook.createSheet("Executions", 1);
            createLabel(executionsSheet);
            createExecutionsSheet(caseStudyGroup, executionsSheet);
            autoSizeColumns(executionsSheet);

            WritableSheet aggregateResultsSheet = workbook.createSheet(AGGREGATE_RESULTS, 2);
            createLabel(aggregateResultsSheet);
            createAggregateResultsSheet(caseStudyGroup, aggregateResultsSheet);
            autoSizeColumns(aggregateResultsSheet);

            workbook.write();
            workbook.close();

        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }
    public static final String AGGREGATE_RESULTS = "AggregateResults";

    private static void createCaseDefinitionSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {

        int column = 0;
        int row = 0;

        //Create table for GRS
        {
            GroupRecommenderSystem<Object, Object> groupRecommenderSystem = caseStudyGroup.getGroupRecommenderSystem();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Group Recommender System");
            row++;
            addText(sheet, column, row, groupRecommenderSystem.getName());
            row++;
            for (Parameter parameter : groupRecommenderSystem.getParameters()) {
                Object parameterValue = groupRecommenderSystem.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for DatasetLoader
        {
            DatasetLoader<? extends Rating> datasetLoader = caseStudyGroup.getDatasetLoader();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, DATASET_LOADER_CELL_CONTENT);
            row++;
            addText(sheet, column, row, datasetLoader.getName());
            row++;
            for (Parameter parameter : datasetLoader.getParameters()) {
                Object parameterValue = datasetLoader.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for GroupFormationTechnique
        {
            GroupFormationTechnique groupFormationTechnique = caseStudyGroup.getGroupFormationTechnique();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Group Formation Technique");
            row++;
            addText(sheet, column, row, groupFormationTechnique.getName());
            row++;
            for (Parameter parameter : groupFormationTechnique.getParameters()) {
                Object parameterValue = groupFormationTechnique.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for GroupValidationTechnique
        {
            GroupValidationTechnique groupValidationTechnique = caseStudyGroup.getGroupValidationTechnique();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Group Validation Technique");
            row++;
            addText(sheet, column, row, groupValidationTechnique.getName());
            row++;
            for (Parameter parameter : groupValidationTechnique.getParameters()) {
                Object parameterValue = groupValidationTechnique.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for GroupPredictionProtocol
        {
            GroupPredictionProtocol groupPredictionProtocol = caseStudyGroup.getGroupPredictionProtocol();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Group Prediction Protocol");
            row++;
            addText(sheet, column, row, groupPredictionProtocol.getName());
            row++;
            for (Parameter parameter : groupPredictionProtocol.getParameters()) {
                Object parameterValue = groupPredictionProtocol.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for RelevanceCriteria
        {
            RelevanceCriteria relevanceCriteria = caseStudyGroup.getRelevanceCriteria();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Relevance Criteria threshold >= " + relevanceCriteria.getThreshold().doubleValue());
            row++;
        }
    }
    public static final String DATASET_LOADER_CELL_CONTENT = "Dataset Loader";

    final static int parameterNameOffset = 1;
    final static int parameterTypeOffset = 2;
    final static int parameterValueOffset = 3;

    /**
     *
     * @param parameter
     * @param parameterValue
     * @param sheet
     * @param column
     * @param row
     * @return Devuelve la fila por la que se debe seguir escribiendo.
     * @throws WriteException
     */
    private static int writeParameterAndValue(Parameter parameter, Object parameterValue, WritableSheet sheet, int column, int row) throws WriteException {

        //First write the parameter line
        addText(sheet, column, row, "Parameter");
        addText(sheet, column + parameterNameOffset, row, parameter.getName());
        addText(sheet, column + parameterTypeOffset, row, parameter.getRestriction().getName());

        if (parameterValue instanceof ParameterOwner) {
            ParameterOwner parameterOwner = (ParameterOwner) parameterValue;
            addText(sheet, column + parameterValueOffset, row, parameterOwner.getName());
        } else {
            if (parameterValue instanceof java.lang.Number) {

                if ((parameterValue instanceof java.lang.Integer) || (parameterValue instanceof java.lang.Long)) {
                    java.lang.Long number = ((java.lang.Number) parameterValue).longValue();
                    addNumber(sheet, column + parameterValueOffset, row, number);
                } else {
                    java.lang.Number number = (java.lang.Number) parameterValue;
                    addNumber(sheet, column + parameterValueOffset, row, number.doubleValue());
                }
            } else {
                addText(sheet, column + parameterValueOffset, row, parameterValue.toString());
            }
        }

        //Then, if it is a parameter owner, write its children parameters.
        if (parameterValue instanceof ParameterOwner) {
            column++;
            ParameterOwner parameterOwner = (ParameterOwner) parameterValue;
            for (Parameter innerParameter : parameterOwner.getParameters()) {
                row++;
                Object innerParameterValue = parameterOwner.getParameterValue(innerParameter);
                row = writeParameterAndValue(innerParameter, innerParameterValue, sheet, column, row);
            }
            column--;
        }

        return row;
    }

    final static int maxListSize = 20;

    private static void createExecutionsSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {

        int row = 0;

        final int numExecutions = caseStudyGroup.getNumExecutions();
        final int numSplits = caseStudyGroup.getGroupValidationTechnique().getNumberOfSplits();

        final int vueltaColumn = 0;
        final int executionColumn = 1;
        final int splitColumn = 2;
        /**
         * Numero de recomendaciones que se consideran para la precisión.
         */

        //Escribo los titulos de las columnas.
        addTitleText(sheet, vueltaColumn, row, "#");
        addTitleText(sheet, executionColumn, row, "Execution");
        addTitleText(sheet, splitColumn, row, "Split");

        PRSpaceGroups pRSpaceGroups = null;
        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, GroupEvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = splitColumn + 1;
            for (GroupEvaluationMeasure groupEvaluationMeasure : caseStudyGroup.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);

                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);

                if (groupEvaluationMeasure instanceof PRSpaceGroups) {
                    pRSpaceGroups = (PRSpaceGroups) groupEvaluationMeasure;
                    for (int listSize = 1; listSize <= maxListSize; listSize++) {
                        indexOfMeasures.put("Precision@" + listSize, i++);
                    }
                }
            }
            indexOfMeasures.put("BuildTime", i++);
            indexOfMeasures.put("GroupModelBuildTime", i++);
            indexOfMeasures.put("RecommendationTime", i++);
        }

        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();
            addTitleText(sheet, column, row, name);
        }

        row++;

        int vuelta = 1;
        for (int thisExecution = 0; thisExecution < numExecutions; thisExecution++) {
            for (int thisSplit = 0; thisSplit < numSplits; thisSplit++) {

                //Escribo la linea de esta ejecución concreta
                addNumber(sheet, vueltaColumn, row, vuelta);
                addNumber(sheet, executionColumn, row, thisExecution + 1);
                addNumber(sheet, splitColumn, row, thisSplit + 1);

                //Ahora los valores de cada metrica.
                for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
                    String name = entry.getKey();
                    int column = entry.getValue();

                    final double value;
                    if (name.equals("BuildTime")) {
                        value = caseStudyGroup.getBuildTime(thisExecution, thisSplit);
                    } else {
                        if (name.equals("GroupModelBuildTime")) {
                            value = caseStudyGroup.getGroupBuildTime(thisExecution, thisSplit);
                        } else {
                            if (name.equals("RecommendationTime")) {
                                value = caseStudyGroup.getRecommendationTime(thisExecution, thisSplit);
                            } else {
                                if (name.startsWith("Precision@")) {
                                    GroupMeasureResult measureResult = caseStudyGroup.getMeasureResult(pRSpaceGroups, thisExecution, thisSplit);
                                    Map<String, Double> detailedResult = (Map<String, Double>) measureResult.getDetailedResult();

                                    Double get = detailedResult.get(name);

                                    if (get == null) {
                                        //No se llegan a recomendar tantos productos.
                                        value = Double.NaN;
                                    } else {
                                        value = get;
                                    }
                                } else {
                                    //Es una medida cualquiera.
                                    GroupEvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                                    value = caseStudyGroup.getMeasureResult(groupEvaluationMeasure, thisExecution, thisSplit).getValue();
                                }
                            }
                        }
                    }

                    if (!Double.isNaN(value)) {
                        double decimalTrimmedValue = NumberRounder.round(value, 5);
                        addNumber(sheet, column, row, decimalTrimmedValue);
                    } else {
                        addText(sheet, column, row, "");
                    }
                }

                vuelta++;
                row++;

            }
        }

    }

    private static void createAggregateResultsSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {
        int row = 0;

        PRSpaceGroups pRSpaceGroups = null;
        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, GroupEvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = 0;
            for (GroupEvaluationMeasure groupEvaluationMeasure : caseStudyGroup.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);

                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);

                if (groupEvaluationMeasure instanceof PRSpaceGroups) {
                    pRSpaceGroups = (PRSpaceGroups) groupEvaluationMeasure;
                    for (int listSize = 1; listSize <= maxListSize; listSize++) {
                        indexOfMeasures.put("Precision@" + listSize, i++);
                    }
                }
            }
            indexOfMeasures.put("BuildTime", i++);
            indexOfMeasures.put("GroupModelBuildTime", i++);
            indexOfMeasures.put("RecommendationTime", i++);
        }

        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();
            addTitleText(sheet, column, row, name);
        }

        row++;

        //Ahora los valores agregados de cada metrica.
        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();

            final double value;

            if (name.equals("BuildTime")) {
                value = caseStudyGroup.getAggregateBuildTime();
            } else {
                if (name.equals("GroupModelBuildTime")) {
                    value = caseStudyGroup.getAggregateGroupBuildTime();
                } else {
                    if (name.equals("RecommendationTime")) {
                        value = caseStudyGroup.getAggregateRecommendationTime();
                    } else {
                        if (name.startsWith("Precision@")) {
                            GroupMeasureResult measureResult = caseStudyGroup.getAggregateMeasureResult(pRSpaceGroups);
                            Map<String, Double> detailedResult = (Map<String, Double>) measureResult.getDetailedResult();

                            Double get = detailedResult.get(name);

                            if (get == null) {
                                //No se llegan a recomendar tantos productos.
                                value = Double.NaN;
                            } else {
                                value = get;
                            }
                        } else {
                            //Es una medida cualquiera.
                            GroupEvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                            value = caseStudyGroup.getAggregateMeasureResult(groupEvaluationMeasure).getValue();
                        }
                    }
                }
            }

            if (!Double.isNaN(value)) {
                double decimalTrimmedValue = NumberRounder.round(value, 5);
                addNumber(sheet, column, row, decimalTrimmedValue);
            } else {
                addText(sheet, column, row, "");
            }
            double decimalTrimmedValue = NumberRounder.round(value, 5);

            addNumber(sheet, column, row, decimalTrimmedValue);
        }

    }

    private static void createLabel(WritableSheet sheet)
            throws WriteException {
        initDefaultFormat();
        initDecimalFormat();
        initIntegerFormat();
        initTitleFormat();

    }

    public static void autoSizeColumns(WritableSheet sheet) {
        for (int x = 0; x < 40; x++) {
            CellView cell = sheet.getColumnView(x);
            cell.setAutosize(true);
            sheet.setColumnView(x, cell);
        }
    }

    private static void addTitleText(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, titleFormat);
        sheet.addCell(label);
    }

    private static void addNumber(WritableSheet sheet, int column, int row,
            double value) throws WriteException, RowsExceededException {
        double rounded = NumberRounder.round(value, 8);

        Number number = new Number(column, row, rounded, decimalFormat);
        sheet.addCell(number);
    }

    private static void addNumber(WritableSheet sheet, int column, int row,
            long integer) throws WriteException, RowsExceededException {
        Number number = new Number(column, row, integer, integerFormat);
        sheet.addCell(number);
    }

    private static void addText(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, defaultFormat);
        sheet.addCell(label);
    }
}