package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.ModifiersImageFactory;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Handler for navigation to the aggregated Timer data.
 *
 * @author Ivan Senic
 *
 */
public class NavigateToAggregatedTimerDataHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		Object selectedObject = selection.getFirstElement();
		TimerData dataToNavigateTo = null;
		if (selectedObject instanceof TimerData) {
			dataToNavigateTo = (TimerData) selectedObject;
		} else if (selectedObject instanceof InvocationSequenceData) {
			dataToNavigateTo = ((InvocationSequenceData) selectedObject).getTimerData();
		}

		if (null != dataToNavigateTo) {
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(dataToNavigateTo.getMethodIdent());

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			inputDefinition.setId(SensorTypeEnum.TIMER);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setSensorName(SensorTypeEnum.TIMER.getDisplayName());
			editorPropertiesData.setSensorImage(SensorTypeEnum.TIMER.getImage());
			editorPropertiesData.setViewName(TextFormatter.getMethodString(methodIdent));
			editorPropertiesData.setViewImage(ModifiersImageFactory.getImage(methodIdent.getModifiers()));
			editorPropertiesData.setPartNameFlag(PartType.SENSOR);
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(dataToNavigateTo.getPlatformIdent());
			idDefinition.setMethodId(dataToNavigateTo.getMethodIdent());
			inputDefinition.setIdDefinition(idDefinition);

			// open the view via command
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			Command command = commandService.getCommand(OpenViewHandler.COMMAND);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
			IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
			context.addVariable(OpenViewHandler.INPUT, inputDefinition);

			try {
				command.executeWithChecks(executionEvent);
			} catch (NotDefinedException | NotEnabledException | NotHandledException e) {
				throw new ExecutionException("Error opening the aggregated timer data view.", e);
			}
		}

		return null;
	}
}
