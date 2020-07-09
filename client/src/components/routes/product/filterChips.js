import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import Chip from "@material-ui/core/Chip";
import Box from '@material-ui/core/Box';
import log from "loglevel";
import {
    ADD_SELECTED_CATEGORY

} from "../../../actions/types";

const FilterChips = () => {
    const selectedGenders = useSelector(state => state.selectedFilterAttributesReducer.genders)
    const selectedApparels = useSelector(state => state.selectedFilterAttributesReducer.apparels)
    const selectedBrands = useSelector(state => state.selectedFilterAttributesReducer.brands)
    const selectedPriceRanges = useSelector(state => state.selectedFilterAttributesReducer.prices)
    const dispatch = useDispatch()

    if ((selectedGenders.length + selectedApparels.length
        + selectedBrands.length + selectedPriceRanges.length) === 0) {
        log.debug(`[FilterChips] Filter are empty`)
        return null
    }

    const addBoxTagToList = (selectedAttrList, categoryId) => {
        let chipBoxList = []
        log.debug(`[FilterChips] addBoxTagToList boxDataList = ${JSON.stringify(selectedAttrList)}`)

        selectedAttrList.forEach(({id,value}) => {
            log.debug(`[FilterChips] renderChipBoxes id = ${id}, value = ${value}`)
            chipBoxList.push(
                <Box key={`${categoryId}-${id}`} width="auto" display="inline-block" p={0.2}>
                    <Chip label={value}
                          color="primary"
                          onDelete={handleDelete(`${categoryId}-${id}`)}/>
                </Box>
            )
        })

        return chipBoxList
    }

    const renderChipBoxes = () => {
        log.debug(`[FilterChips] renderChipBoxes is invoked`)

        let chipBoxList = []
        if (selectedGenders.length > 0) {
            chipBoxList = chipBoxList.concat(addBoxTagToList(selectedGenders, "ge"))
        }
        if (selectedApparels.length > 0) {
            chipBoxList = chipBoxList.concat(addBoxTagToList(selectedApparels, "ap"))
        }
        if (selectedBrands.length > 0) {
            chipBoxList = chipBoxList.concat(addBoxTagToList(selectedBrands, "br"))
        }
        if (selectedPriceRanges.length > 0) {
            chipBoxList = chipBoxList.concat(addBoxTagToList(selectedPriceRanges, "pr"))
        }

        if (chipBoxList) {
            log.debug(`[FilterChips] renderChipBoxes chipBoxList = ${chipBoxList}`)
            return chipBoxList
        }

        log.info(`[FilterChips] renderChipBoxes is returning null`)
        return null
    }

    const findValueAndDispatch = (id, selectedAttrList, attributeName) => {
        log.debug(`[FilterChips] findValueAndDispatch id = ${id}`+
            `, attributeName = ${attributeName}, selectedAttrList = ${JSON.stringify(selectedAttrList)}`)

        for (let i = 0; i < selectedAttrList.length; i++) {
            if(selectedAttrList[i].id === parseInt(id)) {
                log.info(`[FilterChips] id = ${id} dispatch`)
                dispatch({
                    type: ADD_SELECTED_CATEGORY,
                    payload: {
                        [attributeName]: {
                            id: selectedAttrList[i].id,
                            value: selectedAttrList[i].value
                        }
                    }
                })
                return
            }
        }
    }

    const handleDelete = id => () => {
        log.info(`[FilterChips] handleDelete for chip for id = ${id}`)
        const splitId = id.split("-")

        if(selectedGenders.length > 0 && splitId[0].localeCompare("ge") === 0) {
            findValueAndDispatch(splitId[1], selectedGenders, "genders")
        }
        if(selectedApparels.length > 0 && splitId[0].localeCompare("ap") === 0) {
            findValueAndDispatch(splitId[1], selectedApparels, "apparels")
        }
        if(selectedBrands.length > 0 && splitId[0].localeCompare("br") === 0) {
            findValueAndDispatch(splitId[1], selectedBrands, "brands")
        }
        if(selectedPriceRanges.length > 0 && splitId[0].localeCompare("pr") === 0) {
            findValueAndDispatch(splitId[1], selectedPriceRanges, "prices")
        }
    }

    log.info(`[FilterChips] Rendering FilterChips Component`)
    return (
        <>
            {renderChipBoxes()}
        </>
    )
};
export default FilterChips;