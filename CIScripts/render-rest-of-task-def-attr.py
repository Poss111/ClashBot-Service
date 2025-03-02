import argparse
import jinja2
import json
import sys
import os

# Load environment variables dynamically into a dictionary
template_vars = {key: value for key, value in os.environ.items() if key.startswith("ECS_")}

def render_template(template_path, context):
    with open(template_path, 'r') as file:
        template = jinja2.Template(file.read())
    return template.render(context)

def main(task_def_file, template_vars):
    
    rendered_content = render_template(task_def_file, template_vars)
    
    output_file = 'rendered_' + task_def_file
    with open(output_file, 'w') as file:
        file.write(rendered_content)
    
    print(f'Rendered content written to {output_file}')
    with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
        print(f'rendered-task-definition={output_file}', file=fh)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python render-rest-of-task-def-attr.py <task_def_file>")
        sys.exit(1)
    
    task_def_file = sys.argv[1]
    
    main(task_def_file, template_vars)