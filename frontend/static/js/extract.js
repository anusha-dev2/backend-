const fs = require('fs');
const path = require('path');
const m = JSON.parse(fs.readFileSync('main.c80d81e0.js.map', 'utf8'));
const outDir = 'C:\\Users\\nagat_6v7mhde\\Downloads\\D_S_UI\\Digital_Signage_UI_Updated_Version-02\\src';

let count = 0;
m.sources.forEach((srcPath, idx) => {
    if (!srcPath.startsWith('../node_modules/') && !srcPath.startsWith('../webpack/')) {
        const fullPath = path.join(outDir, srcPath);
        fs.mkdirSync(path.dirname(fullPath), { recursive: true });
        fs.writeFileSync(fullPath, m.sourcesContent[idx], 'utf8');
        count++;
    }
});
console.log('Extracted ' + count + ' files.');
