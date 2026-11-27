class: Workflow
cwlVersion: v1.2
inputs:
  input_1:
    type: File
    format: http://edamontology.org/format_3728
outputs:
  output_1:
    type: File
    format: http://edamontology.org/format_3244
    outputSource: ToolC_01/output_1
steps:
  ToolA_01:
    run: ToolA.cwl
    in:
      input_1: input_1
    out: [output_1]
  ToolB_01:
    run: ToolB.cwl
    in:
      input_1: ToolA_01/output_1
    out: [output_1]
  ToolC_01:
    run: ToolC.cwl
    in:
      input_1: ToolB_01/output_1
    out: [output_1]
