import os

def replace_in_files(directory, old_str, new_str):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.js') or file.endswith('.map'):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    if old_str in content:
                        print(f"Replacing in {file_path}")
                        content = content.replace(old_str, new_str)
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(content)
                except Exception as e:
                    print(f"Error reading {file_path}: {e}")

replace_in_files(r'C:\Users\nagat_6v7mhde\Downloads\ds_server_code_razorpay_stripe_working_testmode\ds_server_code_razorpay_stripe_working_testmode\frontend\static\js', 'http://localhost:9000', 'http://192.168.0.124:9000')
