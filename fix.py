import sys

file_path = r'c:\Users\nagat_6v7mhde\Downloads\Anusha\ds_server_code_razorpay_stripe_working_testmode 2 (3)\ds_server_code_razorpay_stripe_working_testmode\src\main\java\com\mediaserver\controller\ContentController.java'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

insertion = """                selected = zone2.get(0);
            } else {
                if (zone1 == null || zone1.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                selected = zone1.get(0);
            }

            if (selected == null || selected.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // keep same authorization rules as other /content/{id}/streaming endpoints
            if (!isAuthorized(selected.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Prefer Range-aware streaming for MP4.
            Resource resource = storageService.loadAsResource(selected.getFilePath());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
"""

if 'HttpHeaders headers = new HttpHeaders();' in lines[797]:
    lines.insert(797, insertion)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(lines)
    print('Fixed successfully.')
else:
    print('Failed to find the insertion point at line 798. It was:', lines[797])
