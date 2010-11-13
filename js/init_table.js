var sorter = new TINY.table.sorter('sorter','table',{
	headclass:'head', // Header Class //
	ascclass:'asc', // Ascending Class //
	descclass:'desc', // Descending Class //
	evenclass:'evenrow', // Even Row Class //
	oddclass:'oddrow', // Odd Row Class //
	evenselclass:'evenselected', // Even Selected Column Class //
	oddselclass:'oddselected', // Odd Selected Column Class //
	paginate:true, // Paginate? (true or false) //
	size:10, // Initial Page Size //
	colddid:'columns', // Columns Dropdown ID (optional) //
	currentid:'currentpage', // Current Page ID (optional) //
	totalid:'totalpages', // Current Page ID (optional) //
	startingrecid:'startrecord', // Starting Record ID (optional) //
	endingrecid:'endrecord', // Ending Record ID (optional) //
	totalrecid:'totalrecords', // Total Records ID (optional) //
	hoverid:'selectedrow', // Hover Row ID (optional) //
	pageddid:'pagedropdown', // Page Dropdown ID (optional) //
	navid:'tablenav', // Table Navigation ID (optional) //
	sortcolumn:0, // Index of Initial Column to Sort (optional) //
	sortdir:-1, // Sort Direction (1 or -1) //
	init:true // Init Now? (true or false) //
});

var maps_sorter = new TINY.table.sorter('maps_sorter','map_table',{
	headclass:'head', // Header Class //
	ascclass:'asc', // Ascending Class //
	descclass:'desc', // Descending Class //
	evenclass:'evenrow', // Even Row Class //
	oddclass:'oddrow', // Odd Row Class //
	evenselclass:'evenselected', // Even Selected Column Class //
	oddselclass:'oddselected', // Odd Selected Column Class //
	paginate:true, // Paginate? (true or false) //
	size:1000, // Initial Page Size //
	hoverid:'selectedrow', // Hover Row ID (optional) //
	sortcolumn:1, // Index of Initial Column to Sort (optional) //
	sortdir:1, // Sort Direction (1 or -1) //
	init:true // Init Now? (true or false) //
});
