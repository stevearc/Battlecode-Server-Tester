var matches_sorter = new TINY.table.sorter('matches_sorter','matches_table',{
	headclass:'head', // Header Class //
	ascclass:'asc', // Ascending Class //
	descclass:'desc', // Descending Class //
	evenclass:'evenrow', // Even Row Class //
	oddclass:'oddrow', // Odd Row Class //
	evenselclass:'evenselected', // Even Selected Column Class //
	oddselclass:'oddselected', // Odd Selected Column Class //
	paginate:true, // Paginate? (true or false) //
	size:10, // Initial Page Size //
	colddid:'coldid', // Columns Dropdown ID (optional) //
	currentid:'currentpage', // Current Page ID (optional) //
	totalid:'totalpages', // Current Page ID (optional) //
	startingrecid:'startrecord', // Starting Record ID (optional) //
	endingrecid:'endrecord', // Ending Record ID (optional) //
	totalrecid:'totalrecords', // Total Records ID (optional) //
  hoverid:'selectedrow',
	pageddid:'pagedropdown', // Page Dropdown ID (optional) //
	navid:'tablenav', // Table Navigation ID (optional) //
	sortcolumn:0, // Index of Initial Column to Sort (optional) //
	sortdir:1, // Sort Direction (1 or -1) //
	init:true // Init Now? (true or false) //
});
