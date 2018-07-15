package water.automl.api.schemas3;


import ai.h2o.automl.AutoML;
import ai.h2o.automl.AutoMLBuildSpec;
import hex.schemas.GridSearchSchema;
import hex.schemas.HyperSpaceSearchCriteriaV99;
import water.api.API;
import water.api.Schema;
import water.api.schemas3.*;

// TODO: this is about to change from SchemaV3 to RequestSchemaV3:
public class AutoMLBuildSpecV99 extends SchemaV3<AutoMLBuildSpec, AutoMLBuildSpecV99> {

  //////////////////////////////////////////////////////
  // Input and output classes used by the build process.
  //////////////////////////////////////////////////////

  /**
   * The specification of overall build parameters for the AutoML process.
   * TODO: this should have all the standard early-stopping functionality like Grid does.
   */
  static final public class AutoMLBuildControlV99 extends Schema<AutoMLBuildSpec.AutoMLBuildControl, AutoMLBuildControlV99> {
    public AutoMLBuildControlV99() {
      super();
    }

    @API(help="Optional project name used to group models from multiple AutoML runs into a single Leaderboard; derived from the training data name if not specified.")
    public String project_name;

    @API(help="Loss function (not yet enabled).", direction=API.Direction.INPUT)
    public String loss;

    @API(help="Model performance based stopping criteria for the AutoML run.", direction=API.Direction.INPUT)
    public HyperSpaceSearchCriteriaV99.RandomDiscreteValueSearchCriteriaV99 stopping_criteria;

    @API(help="Number of folds for k-fold cross-validation (defaults to 5, must be >=2 or use 0 to disable). Disabling prevents Stacked Ensembles from being built.", direction=API.Direction.INPUT)
    public int nfolds;

    @API(help="Whether to keep the predictions of the cross-validation predictions. If set to false then running the same AutoML object for repeated runs will cause an exception as CV predictions are required to build additional Stacked Ensemble models in AutoML.", direction=API.Direction.INPUT)
    public boolean keep_cross_validation_predictions;

    @API(help="Whether to keep the cross-validated models. Deleting cross-validation models will save memory in the H2O cluster.", direction=API.Direction.INPUT)
    public boolean keep_cross_validation_models;

  } // class AutoMLBuildControlV99

  /**
   * The specification of the datasets to be used for the AutoML process.
   * The user can specify a directory path, a file path (including HDFS, s3 or the like),
   * or the ID of an already-parsed Frame in the H2O cluster.  Only one of these may be specified;
   * if more than one is specified the server will return a 412.  Paths are processed
   * as usual in H2O.
   * <p>
   * The user also specifies the response column and, optionally, an array of columns to ignore.
   */
  static final public class AutoMLInputV99 extends Schema<AutoMLBuildSpec.AutoMLInput, AutoMLInputV99> {
    public AutoMLInputV99() { super(); }

    @API(help = "Path of training data to import and parse, in any form that H2O accepts, including local files or directories, s3, hdfs, etc.", direction=API.Direction.INPUT)
    public ImportFilesV3 training_path;

    @API(help = "Path of validation data to import and parse, in any form that H2O accepts, including local files or directories, s3, hdfs, etc.", direction=API.Direction.INPUT)
    public ImportFilesV3 validation_path;

    @API(help = "Path of test data (to create the leaderboard) to import and parse, in any form that H2O accepts, including local files or directories, s3, hdfs, etc.", direction=API.Direction.INPUT)
    public ImportFilesV3 leaderboard_path;

    @API(help = "Used to override default settings for training and test data parsing.", direction=API.Direction.INPUT)
    public ParseSetupV3 parse_setup;

    // @API(help="auxiliary relational datasets", direction=API.Direction.INPUT)
    // public String[] datasets_to_join;

    @API(help = "ID of the training data frame.", direction=API.Direction.INPUT)
    public KeyV3.FrameKeyV3 training_frame;

    @API(help = "ID of the validation data frame (used for early stopping in grid searches and for early stopping of the AutoML process itself).", direction=API.Direction.INPUT)
    public KeyV3.FrameKeyV3 validation_frame;

    @API(help = "ID of the leaderboard data frame (used to score models and rank them on the AutoML Leaderboard).", direction=API.Direction.INPUT)
    public KeyV3.FrameKeyV3 leaderboard_frame;

    @API(help = "Response column",
            direction=API.Direction.INPUT,
            is_member_of_frames = {"training_frame", "validation_frame", "leaderboard_frame"},
            is_mutually_exclusive_with = {"ignored_columns", "fold_column", "weights_column"},
            required = false
      )
    public FrameV3.ColSpecifierV3 response_column;

    @API(help = "Fold column (contains fold IDs) in the training frame. These assignments are used to create the folds for cross-validation of the models.",
            direction=API.Direction.INPUT,
            is_member_of_frames = {"training_frame", "validation_frame", "leaderboard_frame"},
            is_mutually_exclusive_with = {"ignored_columns", "response_column", "weights_column"},
            required = false
    )
    public FrameV3.ColSpecifierV3 fold_column;

    @API(help = "Weights column in the training frame, which specifies the row weights used in model training.",
            direction=API.Direction.INPUT,
            is_member_of_frames = {"training_frame", "validation_frame", "leaderboard_frame"},
            is_mutually_exclusive_with = {"ignored_columns", "response_column", "fold_column"},
            required = false
    )
    public FrameV3.ColSpecifierV3 weights_column;

    @API(help = "Names of columns to ignore in the training frame when building models.",
         direction=API.Direction.INPUT,
         is_member_of_frames = {"training_frame", "validation_frame", "leaderboard_frame"},
         is_mutually_exclusive_with = {"response_column", "fold_column", "weights_column"},
         required = false
      )
    public String[] ignored_columns;
  } // class AutoMLInputV99

  static final public class AutoMLFeatureEngineeringV99 extends Schema<AutoMLBuildSpec.AutoMLFeatureEngineering, AutoMLFeatureEngineeringV99> {
    public AutoMLFeatureEngineeringV99() {
      super();
    }

    @API(help="Try frame transformations (boolean; not enabled).", direction=API.Direction.INPUT)
    public boolean try_mutations;
  } // class AutoMLFeatureEngineeringV99

  static final public class AutoMLBuildModelsV99 extends Schema<AutoMLBuildSpec.AutoMLBuildModels, AutoMLBuildModelsV99> {
    public AutoMLBuildModelsV99() {
      super();
    }

    @API(help="A list algorithms to skip during the model-building phase.", values = {"GLM", "DRF", "GBM", "DeepLearning", "StackedEnsemble"}, direction=API.Direction.INPUT)
    public AutoML.algo[] exclude_algos;

    // Disabling until we use/need this
    /*
    @API(help="Optional model build parameter sets, including base hyperparameters and optional hyperparameter search.")
    public GridSearchSchema[] model_searches;
    */
  } // class AutoMLBuildModels

  static final public class AutoMLEnsembleParametersV99 extends Schema<AutoMLBuildSpec.AutoMLEnsembleParameters, AutoMLEnsembleParametersV99> {
    public AutoMLEnsembleParametersV99() {
      super();
    }
  } // class AutoMLEnsembleParametersV99



  ////////////////
  // Input fields

  @API(help="Specification of overall controls for the AutoML build process.", direction=API.Direction.INPUT)
  public AutoMLBuildControlV99 build_control;

  @API(help="Specification of the input data for the AutoML build process.", direction=API.Direction.INPUT)
  public AutoMLInputV99 input_spec;

  @API(help="Specification of the feature engineering for the AutoML build process.", direction=API.Direction.INPUT)
  public AutoMLFeatureEngineeringV99 feature_engineering;

  @API(help="If present, specifies details of how to train models.", direction=API.Direction.INPUT)
  public AutoMLBuildModelsV99 build_models;

  @API(help="If present, AutoML should build ensembles; more control over the process is optional.", direction=API.Direction.INPUT)
  public AutoMLEnsembleParametersV99 ensemble_parameters;

  ////////////////
  // Output fields
  @API(help="The AutoML Job key", direction=API.Direction.OUTPUT)
  public JobV3 job;

}
