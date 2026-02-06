module.exports = {
    "env": {
        "browser": true,
        "commonjs": true,
        "es2021": true
    },
    "extends": "eslint:recommended",
    "parserOptions": {
        "ecmaVersion": 12
    },
	"globals": {
        "Ext": "readonly",
        "bcp": "readonly",
        "eOpts": "writable"
    },
    "rules": {
        "no-unused-vars": ["error", { "vars": "all", "args": "none", "ignoreRestSiblings": false }]
    }
};
